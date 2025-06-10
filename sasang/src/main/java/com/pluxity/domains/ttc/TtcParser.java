package com.pluxity.domains.ttc;

import com.pluxity.domains.station.enums.BusanSubwayStation;
import java.util.*;
import java.util.function.Function;

public class TtcParser {

    // --- 상수 ---
    private static final Map<String, String> OP_CODES =
            Map.of(
                    "B1", "출발",
                    "B2", "도착",
                    "B3", "열차 접근");

    // --- 헬퍼 메서드 ---
    private static String hexToDecimalString(String hexVal) {
        if (hexVal == null || hexVal.isEmpty() || hexVal.equals("-")) return hexVal;
        try {
            return Integer.toString(Integer.parseInt(hexVal, 16));
        } catch (NumberFormatException e) {
            return "변환오류(" + hexVal + ")";
        }
    }

    private static String parseOpCode(String hexVal) {
        if (hexVal == null || hexVal.isEmpty() || hexVal.equals("-")) return hexVal;
        return OP_CODES.getOrDefault(hexVal.toUpperCase(), "알수없는OP(" + hexVal + ")");
    }

    private static String parseTrainDirection(String hexVal) {
        if (hexVal == null || hexVal.isEmpty() || hexVal.equals("-")) return hexVal;
        return switch (hexVal) {
            case "01" -> "상행";
            case "02" -> "하행";
            default -> "알수없는방향(" + hexVal + ")";
        };
    }

    private static String getStationNameByHexCode(String hexStationCode) {
        if (hexStationCode == null || hexStationCode.isEmpty() || hexStationCode.equals("-"))
            return "-";
        try {
            int decimalCode = Integer.parseInt(hexStationCode, 16);
            String formattedCode = String.format("%03d", decimalCode); // BusanSubwayStation 코드는 3자리 문자열
            return BusanSubwayStation.findByCode(formattedCode)
                    .map(BusanSubwayStation::getName)
                    .orElse("역명없음(" + formattedCode + ")");
        } catch (NumberFormatException e) {
            return "역코드변환오류(" + hexStationCode + ")";
        }
    }

    private static String getStationCode(String hexStationCode) { // 역 코드(숫자)만 반환
        if (hexStationCode == null || hexStationCode.isEmpty() || hexStationCode.equals("-"))
            return "-";
        try {
            int decimalCode = Integer.parseInt(hexStationCode, 16);
            return String.format("%03d", decimalCode);
        } catch (NumberFormatException e) {
            return "코드변환오류(" + hexStationCode + ")";
        }
    }

    private static String parseTrainType(String hexVal) {
        if (hexVal == null || hexVal.isEmpty() || hexVal.equals("-")) return hexVal;
        try {
            int decimalValue = Integer.parseInt(hexVal, 16);
            String binaryValue = Integer.toBinaryString(decimalValue);
            // 8비트로 맞추기 (앞에 0 채우기)
            while (binaryValue.length() < 8) {
                binaryValue = "0" + binaryValue;
            }
            // 실제 의미에 따라 변환 필요 (예: 00000001 -> "정기")
            if ("00000001".equals(binaryValue)) return "정기";
            if ("00000010".equals(binaryValue)) return "회송";
            if ("00000100".equals(binaryValue)) return "시운전";
            if ("00001000".equals(binaryValue)) return "입고";
            if ("00010000".equals(binaryValue)) return "출고";
            return "BIN(" + binaryValue + ")"; // 기본값
        } catch (NumberFormatException e) {
            return "종별변환오류(" + hexVal + ")";
        }
    }

    private static final class FieldDefinition {
        final String koreanName;
        final String englishKey;
        final int tableByteIndex; // 실제 데이터 스트림에서의 바이트 인덱스가 아님. 표 기준.
        final int length; // 바이트 단위
        final Function<String, String> parser; // 원본 HEX 값을 받아 파싱된 문자열 반환
        final String remark;
        final boolean isDerived; // 다른 필드 값으로부터 파생되는 필드인가? (예: 역명)
        final String sourceFieldKey; // 파생될 경우, 원본 데이터 필드의 englishKey

        FieldDefinition(
                String koreanName,
                String englishKey,
                int tableByteIndex,
                int length,
                Function<String, String> parser,
                String remark) {
            this(koreanName, englishKey, tableByteIndex, length, parser, remark, false, null);
        }

        FieldDefinition(
                String koreanName,
                String englishKey,
                String sourceFieldKeyForDerivation,
                Function<String, String> parser,
                String remark) {
            this(
                    koreanName,
                    englishKey,
                    -1,
                    0,
                    parser,
                    remark,
                    true,
                    sourceFieldKeyForDerivation); // tableByteIndex, length는 무관
        }

        FieldDefinition(
                String koreanName,
                String englishKey,
                int tableByteIndex,
                int length,
                Function<String, String> parser,
                String remark,
                boolean isDerived,
                String sourceFieldKey) {
            this.koreanName = koreanName;
            this.englishKey = englishKey;
            this.tableByteIndex = tableByteIndex;
            this.length = length;
            this.parser = parser;
            this.remark = remark;
            this.isDerived = isDerived;
            this.sourceFieldKey = sourceFieldKey;
        }
    }

    private static final List<FieldDefinition> FIELD_DEFINITIONS =
            Arrays.asList(
                    new FieldDefinition("데이터 시작 (STX)", "stx", 0, 1, val -> val, "HEX 값 그대로 가져오기"),
                    new FieldDefinition(
                            "데이터 길이",
                            "dataLength",
                            1,
                            2,
                            TtcParser::hexToDecimalString,
                            "HEX 값 그대로 가져오기 (Payload 길이)"),
                    new FieldDefinition("SEQ", "seq", 3, 1, val -> val, "HEX 값 그대로 가져오기"),
                    new FieldDefinition(
                            "OP Code", "opCode", 4, 1, TtcParser::parseOpCode, "B1: 출발, B2: 도착, B3: 열차 접근"),
                    new FieldDefinition("Spare", "spare", 5, 1, val -> val, "HEX 값 그대로 가져오기 (사용하지 않음)"),
                    new FieldDefinition(
                            "년 (Year)", "year", 6, 1, TtcParser::hexToDecimalString, "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "월 (Month)", "month", 7, 1, TtcParser::hexToDecimalString, "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "일 (Day)", "day", 8, 1, TtcParser::hexToDecimalString, "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "시 (Hour)", "hour", 9, 1, TtcParser::hexToDecimalString, "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "분 (Minute)", "minute", 10, 1, TtcParser::hexToDecimalString, "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "초 (Second)", "second", 11, 1, TtcParser::hexToDecimalString, "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "타임스탬프 (Timestamp)",
                            "timestamp",
                            "year",
                            val -> "", // 실제 처리는 별도 로직에서
                            "년월일시분초를 통합한 시간 정보"),
                    new FieldDefinition(
                            "도착역번호 (Arrival Station No)",
                            "arrivalStationCode",
                            12,
                            1,
                            TtcParser::getStationCode,
                            "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "도착역명 (Arrival Station Name)",
                            "arrivalStationName",
                            "arrivalStationCode",
                            TtcParser::getStationNameByHexCode,
                            "도착역번호로 조회"),
                    new FieldDefinition(
                            "열차 운행 방향 (Train Direction)",
                            "trainDirection",
                            13,
                            1,
                            TtcParser::parseTrainDirection,
                            "HEX 값 그대로 가져오기"),
                    new FieldDefinition(
                            "이번열차 종착역번호 (Destination Station No for This Train)",
                            "destStationCodeThisTrain",
                            14,
                            1,
                            TtcParser::getStationCode,
                            "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "이번열차 종착역명 (Destination Station Name for This Train)",
                            "destStationNameThisTrain",
                            "destStationCodeThisTrain",
                            TtcParser::getStationNameByHexCode,
                            "이번열차 종착역번호로 조회"),
                    new FieldDefinition(
                            "다음열차 종착역번호 (Destination Station No for Next Train)",
                            "destStationCodeNextTrain",
                            15,
                            1,
                            TtcParser::getStationCode,
                            "HEX 값을 DEC 값으로 변환"),
                    new FieldDefinition(
                            "다음열차 종착역명 (Destination Station Name for Next Train)",
                            "destStationNameNextTrain",
                            "destStationCodeNextTrain",
                            TtcParser::getStationNameByHexCode,
                            "다음열차 종착역번호로 조회"),
                    new FieldDefinition(
                            "이번 열차 번호 (This Train Number)", "thisTrainNumber", 16, 2, val -> val, "원본 HEX"),
                    new FieldDefinition(
                            "다음 열차 번호 (Next Train Number)", "nextTrainNumber", 18, 2, val -> val, "원본 HEX"),
                    new FieldDefinition(
                            "이번열차 종별 (This Train Type)",
                            "thisTrainType",
                            20,
                            1,
                            TtcParser::parseTrainType,
                            "HEX값을 BIN으로 변환"),
                    new FieldDefinition(
                            "다음열차 종별 (Next Train Type)",
                            "nextTrainType",
                            21,
                            1,
                            TtcParser::parseTrainType,
                            "HEX값을 BIN으로 변환"),
                    new FieldDefinition(
                            "정차 시간 (Stop Time)",
                            "stopTime",
                            22,
                            1,
                            TtcParser::hexToDecimalString,
                            "HEX 값을 DEC 값으로 변환 (OP Code B2일 때만)"),
                    new FieldDefinition("CRC Low", "crcLow", 23, 1, val -> val, "HEX 값 그대로 가져오기"),
                    new FieldDefinition("CRC High", "crcHigh", 24, 1, val -> val, "HEX 값 그대로 가져오기"),
                    new FieldDefinition("데이터 끝 (ETX)", "etx", 25, 1, val -> val, "HEX 값 그대로 가져오기"));

    public static List<ParsedMessage> parse(String hexStream, String line) {
        List<ParsedMessage> allMessages = new ArrayList<>();
        String cleanedHexStream = hexStream.replaceAll("\\s+", "").toUpperCase();
        int streamPointer = 0;
        int messageCount = 0;

        while (streamPointer < cleanedHexStream.length()) {
            int stxIndex = cleanedHexStream.indexOf("02", streamPointer);
            if (stxIndex == -1) break;

            streamPointer = stxIndex;
            messageCount++;
            ParsedMessage currentParsedMessage = new ParsedMessage(messageCount);

            // STX
            if (streamPointer + 2 > cleanedHexStream.length()) {
                /* ... 오류 처리 ... */
                currentParsedMessage.addError("STX 부족");
                allMessages.add(currentParsedMessage);
                break;
            }
            String stx = cleanedHexStream.substring(streamPointer, streamPointer + 2);
            currentParsedMessage.addField(FIELD_DEFINITIONS.get(0), stx, stx);
            streamPointer += 2;

            // Data Length
            if (streamPointer + 4 > cleanedHexStream.length()) {
                /* ... 오류 처리 ... */
                currentParsedMessage.addError("DataLength 부족");
                allMessages.add(currentParsedMessage);
                break;
            }
            String dataLengthHex = cleanedHexStream.substring(streamPointer, streamPointer + 4);
            int declaredPayloadLength;
            FieldDefinition dataLengthDef = FIELD_DEFINITIONS.get(1);
            try {
                declaredPayloadLength = Integer.parseInt(dataLengthHex, 16);
                currentParsedMessage.addField(
                        dataLengthDef, dataLengthHex, dataLengthDef.parser.apply(dataLengthHex));
            } catch (NumberFormatException e) {
                currentParsedMessage.addError("DataLength 형식오류: " + dataLengthHex);
                currentParsedMessage.addField(dataLengthDef, dataLengthHex, "형식오류");
                allMessages.add(currentParsedMessage);
                streamPointer = stxIndex + 2; // 다음 STX부터 다시 시도
                continue;
            }
            streamPointer += 4;

            String opCodeHexValue = "";
            boolean stopTimeFieldExpected = false;
            int payloadProcessingPointer = streamPointer; // 페이로드 시작점 (SEQ)

            // 핵심 필드들 파싱 (STX, Data Length 제외, StopTime, CRC, ETX 제외)
            for (FieldDefinition def : FIELD_DEFINITIONS) {
                if (def.tableByteIndex < 3
                        || def.isDerived
                        || def.englishKey.equals("stopTime")
                        || def.englishKey.equals("crcLow")
                        || def.englishKey.equals("crcHigh")
                        || def.englishKey.equals("etx")) {
                    continue;
                }

                if (payloadProcessingPointer + def.length * 2 > cleanedHexStream.length()) {
                    currentParsedMessage.addError(def.koreanName + " 파싱 중 데이터 부족");
                    currentParsedMessage.addField(def, "N/A", "데이터 부족");
                    break; // 현재 메시지 파싱 중단
                }
                String fieldHex =
                        cleanedHexStream.substring(
                                payloadProcessingPointer, payloadProcessingPointer + def.length * 2);
                String parsedValue;
                try {
                    parsedValue = def.parser.apply(fieldHex);
                    if (def.englishKey.equals("opCode")) {
                        opCodeHexValue = fieldHex.toUpperCase();
                        if ("B2".equals(opCodeHexValue)) {
                            stopTimeFieldExpected = true;
                        }
                    }
                    currentParsedMessage.addField(def, fieldHex, parsedValue);
                } catch (Exception e) {
                    parsedValue = "파싱 오류(" + fieldHex + "): " + e.getMessage();
                    currentParsedMessage.addError(def.koreanName + " 파싱 오류: " + e.getMessage());
                    currentParsedMessage.addField(def, fieldHex, parsedValue); // 오류 값이라도 추가
                }
                payloadProcessingPointer += def.length * 2;
            }

            if (currentParsedMessage.hasErrors()
                    && !containsOnlyEtxError(currentParsedMessage.getErrors())) {
                allMessages.add(currentParsedMessage);
                streamPointer = stxIndex + 2;
                continue;
            }

            // 데이터 길이 유효성 검사
            int coreDataLength = 19; // SEQ부터 다음열차 종별까지의 바이트 수
            int expectedPayloadLengthInField = coreDataLength + (stopTimeFieldExpected ? 1 : 0);
            if (declaredPayloadLength != expectedPayloadLengthInField) {
                currentParsedMessage.addError(
                        String.format(
                                "데이터 길이 불일치: 선언된 길이 %d, OP Code('%s') 기반 계산된 페이로드 길이 %d",
                                declaredPayloadLength, opCodeHexValue, expectedPayloadLengthInField));
            }

            // 파생 필드(역명 등) 처리
            for (FieldDefinition def : FIELD_DEFINITIONS) {
                if (def.isDerived) {
                    if (def.englishKey.equals("timestamp")) {
                        // 시간 정보 통합 처리
                        String timestamp = buildTimestamp(currentParsedMessage);
                        currentParsedMessage.addField(def, "DERIVED", timestamp);
                    } else {
                        ParsedField sourceField = currentParsedMessage.getFields().get(def.sourceFieldKey);
                        if (sourceField != null
                                && sourceField.originalHex != null
                                && !sourceField.originalHex.equals("-")) {
                            try {
                                String derivedValue =
                                        def.parser.apply(sourceField.originalHex); // 파서는 원본 HEX를 받도록 수정됨
                                currentParsedMessage.addField(
                                        def, sourceField.originalHex, derivedValue); // 원본은 참조값, 파싱은 파생값
                            } catch (Exception e) {
                                currentParsedMessage.addField(
                                        def, sourceField.originalHex, "파생오류: " + e.getMessage());
                                currentParsedMessage.addError(def.koreanName + " 파생 오류: " + e.getMessage());
                            }
                        } else {
                            currentParsedMessage.addNullField(def); // 소스 필드가 없거나 "-" 이면 파생 필드도 null 처리
                        }
                    }
                }
            }

            // 정차 시간 처리
            FieldDefinition stopTimeDef =
                    FIELD_DEFINITIONS.stream()
                            .filter(fd -> fd.englishKey.equals("stopTime"))
                            .findFirst()
                            .get();
            if (stopTimeFieldExpected) {
                if (payloadProcessingPointer + stopTimeDef.length * 2 <= cleanedHexStream.length()) {
                    String fieldHex =
                            cleanedHexStream.substring(
                                    payloadProcessingPointer, payloadProcessingPointer + stopTimeDef.length * 2);
                    String parsedValue;
                    try {
                        parsedValue = stopTimeDef.parser.apply(fieldHex);
                    } catch (Exception e) {
                        parsedValue = "파싱 오류";
                        currentParsedMessage.addError(stopTimeDef.koreanName + " 파싱 오류");
                    }
                    currentParsedMessage.addField(stopTimeDef, fieldHex, parsedValue);
                    payloadProcessingPointer += stopTimeDef.length * 2;
                } else {
                    currentParsedMessage.addError(stopTimeDef.koreanName + " 파싱 중 데이터 부족 (B2인데 없음)");
                    currentParsedMessage.addField(stopTimeDef, "N/A", "데이터 부족");
                }
            } else {
                currentParsedMessage.addNullField(stopTimeDef);
            }

            streamPointer = payloadProcessingPointer; // CRC 시작점으로 포인터 이동

            // CRC Low, CRC High, ETX
            for (String key : List.of("crcLow", "crcHigh", "etx")) {
                FieldDefinition def =
                        FIELD_DEFINITIONS.stream().filter(fd -> fd.englishKey.equals(key)).findFirst().get();
                if (streamPointer + def.length * 2 > cleanedHexStream.length()) {
                    currentParsedMessage.addError(def.koreanName + " 파싱 중 데이터 부족");
                    currentParsedMessage.addField(def, "N/A", "데이터 부족");
                    break;
                }
                String fieldHex = cleanedHexStream.substring(streamPointer, streamPointer + def.length * 2);
                String parsedValue = fieldHex;
                if (def.englishKey.equals("etx") && !"03".equals(fieldHex)) {
                    parsedValue = fieldHex + " (오류: ETX 불일치!)";
                    currentParsedMessage.addError("ETX 불일치: " + fieldHex + ", 기대값: 03");
                }
                currentParsedMessage.addField(def, fieldHex, parsedValue);
                streamPointer += def.length * 2;
            }

            // line 정보 추가 (별도의 FieldDefinition 없이 직접 추가)
            currentParsedMessage.addField(
                    new FieldDefinition("라인 정보", "line", -1, 0, val -> val, "라인 정보"), line, line);

            allMessages.add(currentParsedMessage);
        }
        return allMessages;
    }

    private static boolean containsOnlyEtxError(List<String> errors) {
        if (errors == null || errors.isEmpty()) return false;
        return errors.stream().allMatch(e -> e.startsWith("ETX 불일치"));
    }

    public static class ParsedMessage {
        private final int messageNumber;
        private final Map<String, ParsedField> fields;
        private final List<String> errors;

        public ParsedMessage(int messageNumber) {
            this.messageNumber = messageNumber;
            this.fields = new LinkedHashMap<>();
            this.errors = new ArrayList<>();
        }

        public void addField(FieldDefinition def, String originalHex, String parsedValue) {
            this.fields.put(def.englishKey, new ParsedField(originalHex, parsedValue, def.remark));
        }

        public void addNullField(FieldDefinition def) {
            this.fields.put(
                    def.englishKey, new ParsedField("-", null, def.remark + " (조건부 필드, 현재 메시지엔 없음)"));
        }

        public void addError(String errorMsg) {
            this.errors.add(errorMsg);
        }

        public boolean hasErrors() {
            return !this.errors.isEmpty();
        }

        public List<String> getErrors() {
            return this.errors;
        }

        public int getMessageNumber() {
            return messageNumber;
        }

        public Map<String, ParsedField> getFields() {
            return fields;
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"messageNumber\": ").append(this.messageNumber);

            if (hasErrors()) {
                sb.append(",\n  \"errors\": [");
                for (int i = 0; i < errors.size(); i++) {
                    sb.append("\"").append(errors.get(i).replace("\"", "\\\"")).append("\"");
                    if (i < errors.size() - 1) sb.append(", ");
                }
                sb.append("]");
            }

            List<String> fieldsToInclude =
                    List.of(
                            "opCode",
                            "arrivalStationCode",
                            "arrivalStationName",
                            "trainDirection",
                            "thisTrainNumber",
                            "nextTrainNumber",
                            "timestamp",
                            "line");

            String opCodeHex =
                    Optional.ofNullable(this.fields.get("opCode"))
                            .map(pf -> pf.originalHex)
                            .orElse("")
                            .toUpperCase();

            for (String fieldKey : fieldsToInclude) {
                ParsedField pf = this.fields.get(fieldKey);
                sb.append(",\n");

                if (pf != null) {
                    if (fieldKey.equals("stopTime") && !"B2".equals(opCodeHex)) {
                        sb.append("  \"").append(fieldKey).append("\": null");
                    } else {
                        String valueStr =
                                (pf.parsedValue == null) ? "null" : "\"" + escapeJsonString(pf.parsedValue) + "\"";
                        sb.append("  \"").append(fieldKey).append("\": ").append(valueStr);
                    }
                } else {
                    if (fieldKey.equals("stopTime") && !"B2".equals(opCodeHex)) {
                        sb.append("  \"").append(fieldKey).append("\": null");
                    } else {
                        sb.append("  \"").append(fieldKey).append("\": null");
                    }
                }
            }
            sb.append("\n}");
            return sb.toString();
        }

        private String escapeJsonString(String value) {
            if (value == null) return null;
            return value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    private static class ParsedField {
        final String originalHex;
        final String parsedValue;
        final String remark;

        ParsedField(String originalHex, String parsedValue, String remark) {
            this.originalHex = originalHex != null ? originalHex : "-";
            this.parsedValue = parsedValue;
            this.remark = remark != null ? remark : "";
        }
    }

    private static String buildTimestamp(ParsedMessage message) {
        try {
            ParsedField yearField = message.getFields().get("year");
            ParsedField monthField = message.getFields().get("month");
            ParsedField dayField = message.getFields().get("day");
            ParsedField hourField = message.getFields().get("hour");
            ParsedField minuteField = message.getFields().get("minute");
            ParsedField secondField = message.getFields().get("second");

            if (yearField == null
                    || monthField == null
                    || dayField == null
                    || hourField == null
                    || minuteField == null
                    || secondField == null) {
                return null;
            }

            String year = yearField.parsedValue;
            String month = monthField.parsedValue;
            String day = dayField.parsedValue;
            String hour = hourField.parsedValue;
            String minute = minuteField.parsedValue;
            String second = secondField.parsedValue;

            // 2자리 년도를 4자리로 변환 (20XX 년도로 가정)
            if (year != null && year.length() <= 2) {
                int yearInt = Integer.parseInt(year);
                year = String.valueOf(2000 + yearInt);
            }

            return String.format(
                    "%s-%02d-%02d %02d:%02d:%02d",
                    year,
                    Integer.parseInt(month),
                    Integer.parseInt(day),
                    Integer.parseInt(hour),
                    Integer.parseInt(minute),
                    Integer.parseInt(second));
        } catch (Exception e) {
            return "타임스탬프 생성 오류: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String hexStream =
                // 1. B1(출발) + 01(상행) - Payload: 19(0x13) bytes
                "02 0013 01 B1 00 18 05 0A 0B 0C 0D 77 01 86 86 2166 2168 01 01 AABB 03 "
                        +
                        // 2. B1(출발) + 02(하행) - Payload: 19(0x13) bytes
                        "02 0013 02 B1 00 18 05 0A 0B 0C 0E 77 02 87 87 2167 2169 01 01 CCDD 03 "
                        +
                        // 3. B2(도착) + 01(상행) - Payload: 20(0x14) bytes. StopTime(0E -> 14초) 추가됨.
                        "02 0014 03 B2 00 18 05 0A 0B 0C 0F 77 01 88 88 2168 216A 01 01 0E EEFF 03 "
                        +
                        // 4. B2(도착) + 02(하행) - Payload: 20(0x14) bytes. StopTime(1F -> 31초) 추가됨.
                        "02 0014 04 B2 00 18 05 0A 0B 0C 10 77 02 89 89 2169 216B 01 01 1F A1B2 03 "
                        +
                        // 5. B3(접근) + 01(상행) - Payload: 19(0x13) bytes
                        "02 0013 05 B3 00 18 05 0A 0B 0C 11 77 01 8A 8A 216A 216C 01 01 C3D4 03 "
                        +
                        // 6. B3(접근) + 02(하행) - Payload: 19(0x13) bytes
                        "02 0013 06 B3 00 18 05 0A 0B 0C 12 77 02 8B 8B 216B 216D 01 01 E5F6 03";
        //                "02 00 13 D4 B3 00 17 04 1A 0E 29 15 77 02 86 86 21 66 21 68 01 01 47 FD 03"
        //                        + "02 00 13 3E B3 00 19 06 02 0E 02 09 77 02 86 86 21 54 21 56 01 01
        // 22 73 03"
        //                        + "02 00 14 3F B2 00 19 06 02 0E 02 0A 7C 01 5F 5F 11 67 11 69 01 01
        // 14 17 D0 03";

        List<ParsedMessage> parsedMessages = parse(hexStream, "1");

        System.out.println("[");
        for (int i = 0; i < parsedMessages.size(); i++) {
            System.out.println(parsedMessages.get(i).toJson());
            if (i < parsedMessages.size() - 1) {
                System.out.println(",");
            }
        }
        System.out.println("]");
    }
}
