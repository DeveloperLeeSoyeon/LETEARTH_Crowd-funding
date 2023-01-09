package com.letearth.openbank.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.letearth.openbank.domain.AccountSearchRequestVO;
import com.letearth.openbank.domain.AccountSearchResponseVO;
import com.letearth.openbank.domain.DepositRequestVO;
import com.letearth.openbank.domain.DepositResponseVO;
import com.letearth.openbank.domain.DepositVO;
import com.letearth.openbank.domain.RequestTokenVO;
import com.letearth.openbank.domain.ResponseTokenVO;
import com.letearth.openbank.domain.TransferResultRequestVO;
import com.letearth.openbank.domain.TransferResultResponseVO;
import com.letearth.openbank.domain.UserInfoRequestVO;
import com.letearth.openbank.domain.UserInfoResponseVO;
import com.letearth.openbank.domain.WithdrawRequestVO;
import com.letearth.openbank.domain.WithdrawResponseVO;


@Service
public class OpenBankApiClient {
	private String client_id="be6aa2c4-8b34-4401-9c54-49643df0e522";
	private String client_secret="b1a25933-1cb9-4ef0-ba24-74020c8fc412";
	private String redirect_uri="http://localhost:8080/openbanking/callback";
	private String grant_type="authorization_code";
	
	
	private static final Logger mylog = LoggerFactory.getLogger(OpenBankApiClient.class);
	
	// 기본 주소
	private String baseUrl = "https://testapi.openbanking.or.kr/v2.0";
		
	private HttpHeaders httpHeaders;
	
	//REST방식 API요청
	private RestTemplate restTemplate;

	// httpheader에 토큰값 담아가기 위해서 헤더에 토큰추가하는 메서드 정의
		public HttpHeaders setHeaderAccessToken(String access_token) {
			httpHeaders.add("Authorization", "Bearer " + access_token);
			
			return httpHeaders;
		}
	
	//토큰발급 요청 메서드
	public ResponseTokenVO requestToken(RequestTokenVO requestTokenVO) {
//		return openBankingApiClient.requestToken(requestTokenVO);
		
		restTemplate = new RestTemplate();
		// http header 지정
		httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		
		//requestTokenVo code => 저장
		requestTokenVO.setRequestToken(client_id, client_secret, redirect_uri, grant_type);
		
		//application/x..~~ 객체저장 불가
		//파라미터 저장해서 들고감
		MultiValueMap<String, String> parameters= new LinkedMultiValueMap<String, String>();
		parameters.add("code", requestTokenVO.getCode());
		parameters.add("client_id", requestTokenVO.getClient_id());
		parameters.add("client_secret",	requestTokenVO.getClient_secret());
		parameters.add("redirect_uri", requestTokenVO.getRedirect_uri());
		parameters.add("grant_type", requestTokenVO.getGrant_type());
		
//		httpHeaders,parameters 담아서 감 => HttpEntity	
		HttpEntity<MultiValueMap<String, String>> param = 
				new HttpEntity<MultiValueMap<String,String>>(parameters,httpHeaders);
		
		
		String requestUrl = "https://testapi.openbanking.or.kr/oauth/2.0/token";
		
		
		
		return restTemplate.exchange(requestUrl,
				HttpMethod.POST,param,ResponseTokenVO.class).getBody();
	}
	
	// 사용자 정보 조회
	public UserInfoResponseVO findUser(UserInfoRequestVO userInfoRequestVO) {
		/// REST 방식 요청에 필요한 객체 생성
		restTemplate = new RestTemplate();
		httpHeaders = new HttpHeaders();
		
		// 2.2.1 사용자정보조회 API URL 주소 생성
		String url = baseUrl + "/user/me";
		
		// HttpHeaders 와 HttpBody 오브젝트를 하나의 객체로 관리하기 위한 HttpEntity 객체 생성
		// => 파라미터로 HttpHeaders 객체 전달을 위해 
		//    헤더 생성 작업을 수행하는 사용자 정의 메서드 setHeaderAccessToken() 호출
		//    (파라미터로 엑세스 토큰 전달 => UserInfoRequestVO 객체에 저장되어 있음)
		HttpEntity<String> openBankingUserInfoRequest = 
				new HttpEntity<String>(setHeaderAccessToken(userInfoRequestVO.getAccess_token()));
		
		System.out.println("사용자조회 서비스 토큰 : " + userInfoRequestVO.getAccess_token());
		
		// UriComponentsBuilder 클래스의 fromHttpUrl() 메서드를 호출하여 URL 파라미터 정보 생성
		// 1단계. UriComponentsBuilder.fromHttpUrl() 메서드를 호출하여 요청 URL 주소 전달
		// 2단계. 1단계에서 생성된 객체의 queryParam() 메서드를 호출하여 전달할 파라미터를
		//        키, 값 형식으로 전달
		// 3단계. 2단계에서 생성된 객체의 build() 메서드를 호출하여 UriComponents 객체 리턴(생성)
		// 위의 세 과정을 빌더 패턴(Builder Pattern)을 활용하여 하나의 문장으로 압축 가능
		// (자기 자신을 리턴하는 메서드 호출 후 연쇄적으로 메서드를 이어나가는 것)
		UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("user_seq_no", userInfoRequestVO.getUser_seq_no())
				.build();
		
		System.out.println("사용자조회 서비스 시퀀스넘버 : " + userInfoRequestVO.getUser_seq_no());
		
		// exchange() 메서드 파라미터 : UriBuilder 문자열로 변환, 요청방식, HttpEntity 객체,
		//                              응답데이터를 파싱하기 위한 클래스(.class 필수)
		// => 메서드 뒤에 .getBody() 메서드를 호출하여 body 데이터에 대한 파싱된 결과를 리턴받기
		return restTemplate.exchange(uriBuilder.toString(), HttpMethod.GET, openBankingUserInfoRequest, UserInfoResponseVO.class).getBody();
	}

	//계좌 조회
	public AccountSearchResponseVO findAccount(AccountSearchRequestVO accountSearchRequestVO) {
		/// REST 방식 요청에 필요한 객체 생성
		restTemplate = new RestTemplate();
		httpHeaders = new HttpHeaders();
		
		// 2.2.1 사용자정보조회 API URL 주소 생성
		String url = baseUrl + "/account/list";
		httpHeaders.add("Authorization", "Bearer " + accountSearchRequestVO.getAccess_token());
		
		HttpEntity<String> openBankingAccountListRequest = new HttpEntity<String>(httpHeaders);
		
		UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
				.queryParam("user_seq_no", accountSearchRequestVO.getUser_seq_no())
				.queryParam("include_cancel_yn", accountSearchRequestVO.getInclude_cancel_yn())
				.queryParam("sort_order", accountSearchRequestVO.getSort_order())
				.build();

		return restTemplate.exchange(uriBuilder.toString(), HttpMethod.GET, openBankingAccountListRequest, AccountSearchResponseVO.class).getBody();
	}
	
	
	
	
	// 출금이체
	public WithdrawResponseVO withdraw( WithdrawRequestVO withdrawRequestVO) {
	
		restTemplate = new RestTemplate();
		httpHeaders = new HttpHeaders();
		
		httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
		

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("bank_tran_id", withdrawRequestVO.getBank_tran_id());
		parameters.put("cntr_account_type", withdrawRequestVO.getCntr_account_type());
		parameters.put("cntr_account_num", withdrawRequestVO.getCntr_account_num());
		parameters.put("dps_print_content", withdrawRequestVO.getDps_print_content());
		parameters.put("fintech_use_num", withdrawRequestVO.getFintech_use_num());
		parameters.put("tran_amt", withdrawRequestVO.getTran_amt());
		parameters.put("tran_dtime", withdrawRequestVO.getTran_dtime());
		parameters.put("req_client_name", withdrawRequestVO.getReq_client_name());
		parameters.put("req_client_num", withdrawRequestVO.getReq_client_num());
		parameters.put("transfer_purpose", withdrawRequestVO.getTransfer_purpose());
		parameters.put("req_client_bank_code", withdrawRequestVO.getReq_client_bank_code());
		parameters.put("req_client_account_num", withdrawRequestVO.getReq_client_account_num());
		//parameters.put("req_client_fintech_use_num", withdrawRequestVO.getReq_client_fintech_use_num());
		parameters.put("recv_client_name", withdrawRequestVO.getRecv_client_name());
		parameters.put("recv_client_bank_code", withdrawRequestVO.getRecv_client_bank_code());
		parameters.put("recv_client_account_num", withdrawRequestVO.getRecv_client_account_num());
		
		System.out.println("출금이체@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+parameters);
		
		//헤더에 토큰넣기
		HttpEntity<Map<String, String>> param = new HttpEntity<Map<String, String>>(parameters, setHeaderAccessToken(withdrawRequestVO.getAccess_token()));
		
		System.out.println("출금이체+헤더토큰@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" +param);
		
		String requestUrl = "https://testapi.openbanking.or.kr/v2.0/transfer/withdraw/fin_num";
		
		
		
		return restTemplate.exchange(requestUrl,HttpMethod.POST, param, WithdrawResponseVO.class).getBody();
	}	
			
	
	
	
			
	//관리자 -> 판매자 정산 입금이체
	public DepositResponseVO depositRq(DepositRequestVO depositRequestVO) {

		restTemplate = new RestTemplate();
		httpHeaders = new HttpHeaders();
		
		httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
		
		
		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("cntr_account_type", depositRequestVO.getCntr_account_type());
		parameters.put("cntr_account_num", depositRequestVO.getCntr_account_num());
		parameters.put("wd_pass_phrase", depositRequestVO.getWd_pass_phrase());
		parameters.put("wd_print_content", depositRequestVO.getWd_print_content());
		parameters.put("name_check_option", depositRequestVO.getName_check_option());
		parameters.put("tran_dtime", depositRequestVO.getTran_dtime());
		parameters.put("req_cnt", depositRequestVO.getReq_cnt());
		
		Map<String, String> parameters2 = new HashMap<String, String>();

		parameters2.put("tran_no", depositRequestVO.getTran_no());
		parameters2.put("bank_tran_id", depositRequestVO.getBank_tran_id());
		parameters2.put("fintech_use_num", depositRequestVO.getFintech_use_num());
		parameters2.put("print_content", depositRequestVO.getPrint_content());
		parameters2.put("tran_amt", depositRequestVO.getTran_amt());
		parameters2.put("req_client_name", depositRequestVO.getReq_client_name());
		parameters2.put("req_client_bank_code", depositRequestVO.getReq_client_bank_code());
		parameters2.put("req_client_account_num", depositRequestVO.getReq_client_account_num());
		parameters2.put("req_client_num", depositRequestVO.getReq_client_num());
		parameters2.put("transfer_purpose", depositRequestVO.getTransfer_purpose());
		
		List<Map> req_list = new ArrayList<Map>();
		req_list.add(parameters2);
		
		parameters.put("req_list", req_list);
		
		
		System.out.println("입금이체###############################"+parameters);
		
		//헤더에 토큰넣기
		HttpEntity<Map<String, Object>> param = new HttpEntity<Map<String, Object>>(parameters, setHeaderAccessToken(depositRequestVO.getAccess_token()));
		
		System.out.println("입금이체+헤더토큰###############################" + param);
		
		String url = "https://testapi.openbanking.or.kr/v2.0/transfer/deposit/fin_num";
		
		return restTemplate.exchange(url, HttpMethod.POST, param, DepositResponseVO.class).getBody();
	}
	
	
	//이체결과조회
	public TransferResultResponseVO transferResult(TransferResultRequestVO transferResultRequestVO) {
		
		restTemplate = new RestTemplate();
		httpHeaders = new HttpHeaders();
		
		httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
		
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("check_type", transferResultRequestVO.getCheck_type());
		parameters.put("tran_dtime", transferResultRequestVO.getTran_dtime());
		parameters.put("req_cnt", transferResultRequestVO.getReq_cnt());
		
		
		Map<String, String> parameters2 = new HashMap<String, String>();

		parameters2.put("tran_no", transferResultRequestVO.getTran_no());
		parameters2.put("org_bank_tran_id", transferResultRequestVO.getOrg_bank_tran_id());
		parameters2.put("org_bank_tran_date", transferResultRequestVO.getOrg_bank_tran_date());
		parameters2.put("org_tran_amt", transferResultRequestVO.getOrg_tran_amt());
		
		List<Map> req_list = new ArrayList<Map>();
		req_list.add(parameters2);
		
		parameters.put("req_list", req_list);
		
		
		
		System.out.println("결과조회리스트@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+parameters);
		
		//헤더에 토큰넣기
		HttpEntity<Map<String, Object>> param = new HttpEntity<Map<String, Object>>(parameters, setHeaderAccessToken(transferResultRequestVO.getAccess_token()));

		System.out.println("결과조회리스트+헤더토큰@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + param);
		
		String requestUrl = "https://testapi.openbanking.or.kr/v2.0/transfer/result";
		
		return restTemplate.exchange(requestUrl, HttpMethod.POST, param, TransferResultResponseVO.class).getBody();
	}
}
