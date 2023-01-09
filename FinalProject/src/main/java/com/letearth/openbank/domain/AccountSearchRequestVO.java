package com.letearth.openbank.domain;

import lombok.Data;

@Data
public class AccountSearchRequestVO {
    private String access_token;
    private String user_seq_no;
    private String include_cancel_yn; //해지계좌포함여부 Y:해지계좌포함, N:해지계좌불포함
    private String sort_order; //정렬순서주 D:Descending, A:Ascending
    private String model;
    
    public AccountSearchRequestVO() {}
    
	public AccountSearchRequestVO(String access_token, String user_seq_no, String include_cancel_yn, String sort_order,
			String model) {
		super();
		this.access_token = access_token;
		this.user_seq_no = user_seq_no;
		this.include_cancel_yn = include_cancel_yn;
		this.sort_order = sort_order;
		this.model = model;
	}
    
}
