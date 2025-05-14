package com.pluxity.file.service;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.file.dto.SbmFileUploadResponse;
import com.pluxity.file.dto.SbmFloorGroup;
import com.pluxity.file.dto.SbmFloorInfo;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.strategy.storage.S3StorageStrategy;
import com.pluxity.file.strategy.storage.StorageStrategy;
import com.pluxity.global.config.S3Config;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SbmFileService {

    private final StorageStrategy storageStrategy;
    private final S3Config s3Config;
    private final S3Client s3Client;

    @Value("${file.local.path}")
    private String uploadPath;

    public SbmFileUploadResponse processSbmFile(Path tempPath, FileEntity fileEntity) {
        try {
            // 파일 처리 로직
            List<SbmFloorGroup> floorList = new ArrayList<>();
            // 실제 SBM 파일 처리 로직 구현
            // ...

            // 임시 파일 삭제
            Files.deleteIfExists(tempPath);

            return SbmFileUploadResponse.from(fileEntity, floorList);
        } catch (Exception e) {
            log.error("SBM File Processing Error: {}", e.getMessage(), e);
            throw new CustomException(FAILED_TO_PROCESS_SBM_FILE, e.getMessage());
        }
    }

    public SbmFileUploadResponse processSbmFile(FileEntity fileEntity) {
        try {
            // S3 또는 로컬 저장소에서 파일 읽기
            Path tempPath = FileUtils.createTempFile(fileEntity.getOriginalFileName());
            if (storageStrategy instanceof S3StorageStrategy) {
                // S3에서 파일 다운로드
                GetObjectRequest getObjectRequest =
                        GetObjectRequest.builder()
                                .bucket(s3Config.getBucketName())
                                .key(fileEntity.getFilePath())
                                .build();

                ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
                Files.copy(s3Object, tempPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // 로컬에서 파일 복사
                Path sourcePath = Paths.get(uploadPath, fileEntity.getFilePath());
                Files.copy(sourcePath, tempPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 기존 로직 호출
            return processSbmFile(tempPath, fileEntity);
        } catch (Exception e) {
            log.error("SBM File Processing Error: {}", e.getMessage(), e);
            throw new CustomException(FAILED_TO_PROCESS_SBM_FILE, e.getMessage());
        }
    }

    private List<SbmFloorGroup> parseFloors(Path xmlFilePath) {
        Document doc = getDocument(xmlFilePath.toString());
        NodeList floorNodeList = doc.getElementsByTagName("Floor");

        List<SbmFloorInfo> floorInfoList =
                new ArrayList<>(
                        IntStream.range(0, floorNodeList.getLength())
                                .mapToObj(floorNodeList::item)
                                .filter(
                                        floorNode ->
                                                floorNode.getNodeType() == Node.ELEMENT_NODE
                                                        && floorNode.getParentNode().getNodeName().equals("Floors"))
                                .map(Element.class::cast)
                                .map(this::buildSbmFloor)
                                .toList());

        Map<String, List<SbmFloorInfo>> grouped =
                floorInfoList.stream().collect(Collectors.groupingBy(SbmFloorInfo::floorGroup));

        return grouped.entrySet().stream()
                .map(
                        entry -> {
                            String groupId = entry.getKey();
                            List<SbmFloorInfo> floors = entry.getValue();

                            SbmFloorInfo mainFloorOpt =
                                    floors.stream()
                                            .filter(floor -> "True".equalsIgnoreCase(floor.isMain()))
                                            .findFirst()
                                            .orElseThrow(() -> new CustomException(INVALID_SBM_FILE, "메인 층이 존재하지 않습니다"));

                            return new SbmFloorGroup(groupId, mainFloorOpt, floors);
                        })
                .toList();
    }

    private Document getDocument(String xmlFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException e) {
            log.error("XML 파싱 설정 오류: {}", e.getMessage(), e);
            throw new CustomException(INVALID_SBM_FILE, "XML 파싱 설정 오류");
        } catch (SAXException e) {
            log.error("XML 파싱 중 오류: {}", e.getMessage(), e);
            throw new CustomException(INVALID_SBM_FILE, "XML 파싱 중 오류");
        } catch (IOException e) {
            log.error("XML 파일 읽기 오류: {}", e.getMessage(), e);
            throw new CustomException(INVALID_SBM_FILE, "XML 파일 읽기 오류");
        }
    }

    private SbmFloorInfo buildSbmFloor(Element floorElement) {
        String id = floorElement.getAttribute("id");
        String name = floorElement.getAttribute("name");
        String baseFloor = floorElement.getAttribute("baseFloor");
        String groupID = floorElement.getAttribute("groupID");
        String isMain = floorElement.getAttribute("isMain");
        String fileFileName =
                floorElement
                        .getElementsByTagName("FileSource")
                        .item(0)
                        .getAttributes()
                        .getNamedItem("name")
                        .getNodeValue()
                        .substring(2);

        return SbmFloorInfo.builder()
                .floorId(id)
                .floorName(name)
                .fileName(fileFileName)
                .floorBase(baseFloor)
                .floorGroup(groupID)
                .isMain(isMain)
                .build();
    }
}
