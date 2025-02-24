package com.pluxity.file.service;

import com.pluxity.file.dto.SbmFileUploadResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.pluxity.global.constant.ErrorCode.INVALID_SBM_FILE;

@Service
@RequiredArgsConstructor
@Slf4j
public class SbmFileService {

    public SbmFileUploadResponse processSbmFile(Path tempPath, FileEntity entity) {
        try {
            Path unzipDir = FileUtils.createTempDirectory("temp_unzip");

            try (InputStream is = Files.newInputStream(tempPath)) {
                ZipUtils.unzip(is, unzipDir);
            }

            Optional<Path> xmlFileOpt;
            try (Stream<Path> paths = Files.walk(unzipDir)) {
                xmlFileOpt = paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".xml"))
                        .findFirst();
            } catch (IOException e) {
                throw new CustomException(INVALID_SBM_FILE);
            }

            Path xmlPath = xmlFileOpt.orElseThrow(() -> new CustomException(INVALID_SBM_FILE));

            List<SbmFloorInfo> sbmFloorInfoList = parseFloors(xmlPath);

            FileUtils.deleteDirectoryRecursively(unzipDir);

            return new SbmFileUploadResponse(
                    entity.getId(),
                    entity.getOriginalFileName(),
                    entity.getFilePath(),
                    entity.getFileType(),
                    entity.getCreatedAt().toString(),
                    sbmFloorInfoList
            );
        } catch (Exception e) {
            log.error("Failed to process SBM file: {}", e.getMessage());
            throw new CustomException("Failed to process SBM file", HttpStatus.INTERNAL_SERVER_ERROR, "XML 파싱에 실패했습니다");
        }
    }

    private List<SbmFloorInfo> parseFloors(Path xmlFilePath) {
        Document doc = getDocument(xmlFilePath.toString());
        NodeList floorNodeList = doc.getElementsByTagName("Floor");

        return IntStream.range(0, floorNodeList.getLength())
                .mapToObj(floorNodeList::item)
                .filter(floorNode -> floorNode.getNodeType() == Node.ELEMENT_NODE &&
                        floorNode.getParentNode().getNodeName().equals("Floors"))
                .map(Element.class::cast)
                .filter(this::isMain)
                .map(this::buildSbmFloor)
                .toList();
    }

    private Document getDocument(String xmlFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new CustomException(INVALID_SBM_FILE, "XML 파싱 실패");
        }
    }

    private boolean isMain(Element floorElement) {
        return "True".equals(floorElement.getAttribute("isMain"));
    }

    private SbmFloorInfo buildSbmFloor(Element floorElement) {
        String name = floorElement.getAttribute("name");
        String groupId = floorElement.getAttribute("groupID");
        return SbmFloorInfo.builder()
                .name(name)
                .groupId(groupId)
                .build();
    }
}