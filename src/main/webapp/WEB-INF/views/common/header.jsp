<%@page import="kh.semi.comembus.member.model.dto.MemberRole"%>
<%@page import="kh.semi.comembus.member.model.dto.Member"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!-- 미송 코드 시작 -->
<%
	String msg = (String) session.getAttribute("msg");
	System.out.println("session = " + session.getId());
	if(msg != null) session.removeAttribute("msg");
	Member loginMember = (Member) session.getAttribute("loginMember");

	
	String location = request.getHeader("Referer");
	if(location == null){
		location = request.getContextPath() + "/main";
	}
	// System.out.println(location);
	boolean contain = false;
	String[] specialLocation = {"/membus/login", "/membus/enroll", "/membus/findMemberId", "/membus/showMemberId", "/membus/findMemberPassword", "/membus/resetMemberPassword"};
	for(int i = 0; i < specialLocation.length; i++) {
		if(location.contains(specialLocation[i])) {
			contain = true;
			// System.out.println("true>> specialLocation[" + i + "] = " + specialLocation[i]);
		}
	}
	
	Cookie[] cookies = request.getCookies();

	if(!contain) {
		Cookie cookie = new Cookie("locationCookie", location);
		cookie.setPath("/");
		cookie.setMaxAge(24 * 60 * 60);
		response.addCookie(cookie);
		System.out.println("[locationCookie 발급: " + cookie.getValue() + "]");
	}
	
%>
<!-- 미송 코드 끝 -->
    

<!DOCTYPE html>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<!-- 폰트 -->
	<link href="https://font.elice.io/EliceDigitalBaeum.css" rel="stylesheet">
	<link rel="preconnect" href="https://fonts.googleapis.com">
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
	<link href="https://fonts.googleapis.com/css2?family=Nanum+Gothic:wght@400;700&display=swap" rel="stylesheet">
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/reset-css@5.0.1/reset.min.css">
	
	<!-- include libraries(jQuery, bootstrap) -->
	<link href="http://netdna.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.css" rel="stylesheet">
	<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script> 
	<script src="http://netdna.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.js"></script>
	<!-- summer note -->
	<script src="<%=request.getContextPath() %>/js/summernote/summernote-lite.js"></script>
	<script src="<%=request.getContextPath() %>/js/summernote/lang/summernote-ko-KR.js"></script>
	<link rel="stylesheet" href="<%=request.getContextPath() %>/css/summernote/summernote-lite.css">
	
	<link href="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-bs4.min.css" rel="stylesheet">
	<script src="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-bs4.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.1.1/css/all.min.css" integrity="sha512-KfkfwYDsLkIlwQp6LFnl8zNdLGxu9YAA1QvwINks4PhcElQSvqcyVLLD9aMhXd13uQjoXtEKNosOWaZqXgel0g==" crossorigin="anonymous" referrerpolicy="no-referrer" />
	<link rel="stylesheet" href="<%=request.getContextPath() %>/css/style.css">
	
	<link rel="icon" href="favicon.ico" />
	<title>CO;MEMBUS</title>
	
	<script>
	<!-- 미송 코드 시작 -->
	window.onload = () => {
	<% if(msg != null){ %>
		alert("<%= msg %>");
	<% } %>
	};
	<!-- 미송 코드 끝 -->
	
	</script>
	<!-- 수진코드 시작 -->
	<% if(loginMember != null){%>
	<script src="<%=request.getContextPath()%>/js/ws.js"></script>
	<%}%>
	<!-- 수진코드 끝 -->
</head>
<body>
  <header>
    <div class="menubar">
      <div class="h__logo">
        <a href="<%= request.getContextPath() %>/main"><img src="<%= request.getContextPath() %>/images/logo_w.png" alt="logo이미지"></a>
      </div>
  
      <ul class="h__menu__main">
        <li><a href="<%= request.getContextPath() %>/membus/list">멤버스</a></li>
        <li>
          <a href="javascript:void(0)">모임</a>
          <ul class="h__menu__sub">
            <li><a href="<%= request.getContextPath()%>/gathering/projectList">프로젝트</a></li>
            <li><a href="<%= request.getContextPath()%>/gathering/studyList">스터디</a></li>
          </ul>
        </li>
        <li>
          <a href="javascript:void(0)">커뮤니티</a>
          <ul class="h__menu__sub">
            <li><a href="<%= request.getContextPath() %>/community/communityList?co_type=Q">QnA</a></li>
            <li><a href="<%= request.getContextPath() %>/community/communityList?co_type=F">자유주제</a></li>
            <li><a href="<%= request.getContextPath() %>/community/communityList?co_type=S">정보공유</a></li>
          </ul>
        </li>
        <!-- 관리자로그인시 노출 -->
        <% if(loginMember != null && loginMember.getMemberRole() == MemberRole.A){ %>
        <li><a href="<%= request.getContextPath() %>/admin/memberList">회원관리</a></li>
        <li><a href="<%= request.getContextPath() %>/admin/memberEnrollNum">통계관리</a></li>
        <% } %>
      </ul>
  
  <!-- 미송 코드 시작 -->
  <% if(loginMember == null){ %>
      <ul class="h__loginSignup">
        <li><a href="<%= request.getContextPath() %>/membus/login">회원가입/로그인</a></li>
      </ul>
  <% } else {%>  
	  <ul class="h__member__menu">
	  <!-- 닉네임 클릭 시 마이페이지로 이동 -->
		<span class="h__profile-badge">
	  		<b><%= loginMember.getNickName().charAt(0) %></b>
	  	</span>
	  	<li class="h__loginMember">
	  		<a href="<%= request.getContextPath()%>/membus/mypage"><%= loginMember.getNickName() %></a>
	  		<span id="notification">
	  			<a href="<%= request.getContextPath() %>/alerts"><i id="blackBell" class="fa-solid fa-bell bell"></i></a>
	  		</span>
  		</li>	  	
	  	<li class="h__memberLogout"><a href="<%= request.getContextPath() %>/membus/logout">로그아웃</a></li>
	  </ul>
  <% } %>   
  
  <!-- 미송 코드 끝 -->
  
    </div>
  </header>
  <!-- 메인 시작 -->
  <section>