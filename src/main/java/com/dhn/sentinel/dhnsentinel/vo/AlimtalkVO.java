package com.dhn.sentinel.dhnsentinel.vo;

import lombok.Data;

@Data
public class AlimtalkVO {
    // 템플릿 번호
    private String tmp_number;

    // 보내는 사람 번호
    //private String kakao_sender;

    // 받는 사람 번호
    private String kakao_phone;

    // 일단 N
    private String kakao_2nd;

    // 순서대로 변수
    private String kakao_add1;
    private String kakao_add2;
    private String kakao_add3;
    private String kakao_add4;
    private String kakao_add5;
    private String kakao_add6;

    // type 일단 at
    private String mst_type1;
}
