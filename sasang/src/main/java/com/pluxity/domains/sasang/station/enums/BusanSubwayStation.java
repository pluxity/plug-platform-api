package com.pluxity.domains.sasang.station.enums; // Updated package

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusanSubwayStation {
    // 1호선
    DADAEPO_BEACH("095", "다대포해수욕장역", "1호선"),
    DADAEPO_HARBOR("096", "다대포항역", "1호선"),
    NAKGAE("097", "낫개역", "1호선"),
    SINJANGLIM("098", "신장림역", "1호선"),
    JANGLIM("099", "장림역", "1호선"),
    DONGMAE("100", "동매역", "1호선"),
    SINPYEONG("101", "신평역", "1호선"),
    HADAN("102", "하단역", "1호선"),
    DANGRI("103", "당리역", "1호선"),
    SAHA("104", "사하역", "1호선"),
    GOEJEONG("105", "괴정역", "1호선"),
    DAETI("106", "대티역", "1호선"),
    SEODAESIN("107", "서대신역", "1호선"),
    DONGDAESIN("108", "동대신역", "1호선"),
    TOSEONG("109", "토성역", "1호선"),
    JAGALCHI("110", "자갈치역", "1호선"),
    NAMPO("111", "남포역", "1호선"),
    JUNGANG("112", "중앙역", "1호선"),
    BUSAN("113", "부산역", "1호선"),
    CHORYANG("114", "초량역", "1호선"),
    BUSANJIN("115", "부산진역", "1호선"),
    JWACHEON("116", "좌천역", "1호선"),
    BEOMIL("117", "범일역", "1호선"),
    BEOMNAE_GOL("118", "범내골역", "1호선"),
    SEOMYEON_LINE1("119", "서면역", "1호선"),
    BUJEON("120", "부전역", "1호선"),
    YANGJEONG("121", "양정역", "1호선"),
    CITY_HALL("122", "시청역", "1호선"),
    YEONSAN_LINE1("123", "연산역", "1호선"),
    GYODAE("124", "교대역", "1호선"),
    DONGNAE_LINE1("125", "동래역", "1호선"),
    MYEONGNYUN("126", "명륜역", "1호선"),
    ONCHEONJANG("127", "온천장역", "1호선"),
    PUSAN_NATIONAL_UNIV("128", "부산대역", "1호선"),
    JANGJEON("129", "장전역", "1호선"),
    GUSEO("130", "구서역", "1호선"),
    DUSIL("131", "두실역", "1호선"),
    NAMSAN("132", "남산역", "1호선"),
    BEOMEOSA("133", "범어사역", "1호선"),
    NOPO("134", "노포역", "1호선"),

    // 2호선
    JANGSAN("201", "장산역", "2호선"),
    JUNGDONG("202", "중동역", "2호선"),
    HAEUNDAE("203", "해운대역", "2호선"),
    DONGBAEK("204", "동백역", "2호선"),
    BEXCO("205", "벡스코역", "2호선"),
    CENTUM_CITY("206", "센텀시티역", "2호선"),
    MILLAK("207", "민락역", "2호선"),
    SUYEONG_LINE2("208", "수영역", "2호선"),
    GWANGAN("209", "광안역", "2호선"),
    GEUMNYEONSAN("210", "금련산역", "2호선"),
    NAMCHEON("211", "남천역", "2호선"),
    KYUNGSUNG_UNIV_BUKYONG_UNIV("212", "경성대·부경대역", "2호선"),
    DAEYEON("213", "대연역", "2호선"),
    MOTGOL("214", "못골역", "2호선"),
    JIGEGOL("215", "지게골역", "2호선"),
    MUNHYEON("216", "문현역", "2호선"),
    INTL_FINANCE_CENTER_BUSAN_BANK("217", "국제금융센터·부산은행역", "2호선"),
    JEONPO("218", "전포역", "2호선"),
    SEOMYEON_LINE2("219", "서면역", "2호선"),
    BUAM("220", "부암역", "2호선"),
    GAYA("221", "가야역", "2호선"),
    DONG_EUI_UNIV("222", "동의대역", "2호선"),
    GAEGEUM("223", "개금역", "2호선"),
    NAENGJEONG("224", "냉정역", "2호선"),
    JURYE("225", "주례역", "2호선"),
    GAMJEON("226", "감전역", "2호선"),
    SASANG("227", "사상역", "2호선"),
    DEOKPO("228", "덕포역", "2호선"),
    MODEOK("229", "모덕역", "2호선"),
    MORA("230", "모라역", "2호선"),
    GUNAM("231", "구남역", "2호선"),
    GUMYEONG("232", "구명역", "2호선"),
    DEOKCHEON_LINE2("233", "덕천역", "2호선"),
    SUJEONG("234", "수정역", "2호선"),
    HWAMYEONG("235", "화명역", "2호선"),
    YULRI("236", "율리역", "2호선"),
    DONGWON("237", "동원역", "2호선"),
    GEUMGOK("238", "금곡역", "2호선"),
    HOPO("239", "호포역", "2호선"),
    JEUNGSAN("240", "증산역", "2호선"),
    PNU_YANGSAN_CAMPUS("241", "부산대 양산캠퍼스역", "2호선"),
    NAMYANGSAN("242", "남양산역", "2호선"),
    YANGSAN("243", "양산역", "2호선"),

    // 3호선
    SUYEONG_LINE3("301", "수영역", "3호선"),
    MANGMI("302", "망미역", "3호선"),
    BAESAN("303", "배산역", "3호선"),
    MULMANGOL("304", "물만골역", "3호선"),
    YEONSAN_LINE3("305", "연산역", "3호선"),
    GEOJE("306", "거제역", "3호선"),
    SPORTS_COMPLEX("307", "종합운동장역", "3호선"),
    SAJIK("308", "사직역", "3호선"),
    MINAM_LINE3("309", "미남역", "3호선"),
    MANDEOK("310", "만덕역", "3호선"),
    NAMSANJEONG("311", "남산정역", "3호선"),
    SUKDEUNG("312", "숙등역", "3호선"),
    DEOKCHEON_LINE3("313", "덕천역", "3호선"),
    GUPO("314", "구포역", "3호선"),
    GANGSEO_GU_OFFICE("315", "강서구청역", "3호선"),
    SPORTS_PARK("316", "체육공원역", "3호선"),
    DAEJEO("317", "대저역", "3호선"),

    // 4호선
    MINAM_LINE4("401", "미남역", "4호선"),
    DONGNAE_LINE4("402", "동래역", "4호선"),
    SUAN("403", "수안역", "4호선"),
    NAKMIN("404", "낙민역", "4호선"),
    CHUNGNYEOLSA("405", "충렬사역", "4호선"),
    MYEONGJANG("406", "명장역", "4호선"),
    SEODONG("407", "서동역", "4호선"),
    GEUMSA("408", "금사역", "4호선"),
    BANYEO_AGRICULTURAL_MARKET("409", "반여농산물시장역", "4호선"),
    SEOKDAE("410", "석대역", "4호선"),
    YOUNGSAN_UNIV("411", "영산대역", "4호선"),
    WITBANSONG("412", "윗반송역", "4호선"),
    GOCHON("413", "고촌역", "4호선"),
    ANPYEONG("414", "안평역", "4호선");

    private final String code;
    private final String name;
    private final String line;

    public static Optional<BusanSubwayStation> findByCode(String code) {
        return Arrays.stream(values()).filter(station -> station.getCode().equals(code)).findFirst();
    }

    public static List<BusanSubwayStation> findAll() {
        return Arrays.asList(values());
    }

    public Optional<BusanSubwayStation> getPrecedingStation() {
        int currentCode = Integer.parseInt(this.code);
        String precedingCode = String.format("%03d", currentCode - 1);

        return Arrays.stream(values())
                .filter(
                        station ->
                                station.getCode().equals(precedingCode) && station.getLine().equals(this.line))
                .findFirst();
    }

    public Optional<BusanSubwayStation> getFollowingStation() {
        int currentCode = Integer.parseInt(this.code);
        String followingCode = String.format("%03d", currentCode + 1);

        return Arrays.stream(values())
                .filter(
                        station ->
                                station.getCode().equals(followingCode) && station.getLine().equals(this.line))
                .findFirst();
    }
}
