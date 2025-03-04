package com.pluxity.file.service;

import com.pluxity.file.dto.SbmFileUploadResponse;
import com.pluxity.file.dto.SbmFloorGroup;
import com.pluxity.file.dto.SbmFloorInfo;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import com.pluxity.global.utils.ZipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.pluxity.global.constant.ErrorCode.INVALID_SBM_FILE;

@Service
@RequiredArgsConstructor
@Slf4j
public class SbmFileService {

    public SbmFileUploadResponse processSbmFile(Path tempPath, FileEntity entity) {

        Path unzipDir = null;
        try {
            unzipDir = FileUtils.createTempDirectory("temp_unzip");

            try (InputStream is = Files.newInputStream(tempPath)) {
                ZipUtils.unzip(is, unzipDir);
            }

            Optional<Path> xmlFileOpt;
            try (Stream<Path> paths = Files.walk(unzipDir)) {
                xmlFileOpt = paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".xml"))
                        .findFirst();
            } catch (IOException e) {
                log.error("Failed to search xml file: {}", e.getMessage(), e);
                throw new CustomException(INVALID_SBM_FILE);
            }

            Path xmlPath = xmlFileOpt.orElseThrow(() -> new CustomException(INVALID_SBM_FILE, "SBM 파일 내부에 XML 파일이 존재하지 않습니다."));

            List<SbmFloorGroup> sbmFloorGroupList = parseFloors(xmlPath);

            FileUtils.deleteDirectoryRecursively(unzipDir);

            return new SbmFileUploadResponse(
                    entity.getId(),
                    entity.getFilePath(),
                    entity.getOriginalFileName(),
                    entity.getContentType(),
                    entity.getCreatedAt().toString(),
                    sbmFloorGroupList
            );
        } catch (CustomException e) {
            throw e; // CustomException 을 던짐
        } catch (Exception e) {
            log.error("Failed to process SBM file: {}", e.getMessage(), e);
            throw new CustomException(INVALID_SBM_FILE, "XML 파싱에 실패했습니다");
        } finally {
            if(unzipDir!= null){
                try{
                    FileUtils.deleteDirectoryRecursively(unzipDir);
                }catch (IOException e){
                    log.error("Failed to delete unzipDir : {}", e.getMessage(), e);
                }
            }
        }
    }

    private List<SbmFloorGroup> parseFloors(Path xmlFilePath) {
        Document doc = getDocument(xmlFilePath.toString());
        NodeList floorNodeList = doc.getElementsByTagName("Floor");

        List<SbmFloorInfo> floorInfoList = new ArrayList<>(IntStream.range(0, floorNodeList.getLength())
                .mapToObj(floorNodeList::item)
                .filter(floorNode -> floorNode.getNodeType() == Node.ELEMENT_NODE &&
                        floorNode.getParentNode().getNodeName().equals("Floors"))
                .map(Element.class::cast)
                .map(this::buildSbmFloor)
                .toList());

        Map<String, List<SbmFloorInfo>> grouped = floorInfoList.stream()
                .collect(Collectors.groupingBy(SbmFloorInfo::floorGroup));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String groupId = entry.getKey();
                    List<SbmFloorInfo> floors = entry.getValue();

                    SbmFloorInfo mainFloorOpt = floors.stream()
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
        String fileFileName = floorElement.getElementsByTagName("FileSource")
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