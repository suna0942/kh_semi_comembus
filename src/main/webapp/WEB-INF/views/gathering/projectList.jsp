<%@page import="kh.semi.comembus.gathering.model.dto.GatheringExt"%>
<%@page import="kh.semi.comembus.gathering.model.dto.Gathering"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<link rel="stylesheet" href="<%=request.getContextPath() %>/css/gathering/gatheringList.css">  
<link rel="stylesheet" href="https://unpkg.com/swiper@8/swiper-bundle.min.css"/>
<script src="https://unpkg.com/swiper@8/swiper-bundle.min.js"></script>
<script defer src="<%=request.getContextPath() %>/js/gathering/gathering.js"></script>
<script src="<%= request.getContextPath() %>/js/jquery-3.6.0.js"></script>

<script>

const bookmarkFilter = (num) => {
	$("#p__local").val("All").prop("selected", true);
	$("#p__job_code").val("All").prop("selected", true);
	$("#p__status").prop("checked", false);
	
	const bookmarkYN = $("#p__bookmark").is(':checked') ? "Y" : "All";
	let memberId = "";
	<c:if test="${empty loginMember}">
		alert("로그인 후 이용해주세요");
		$("#p__bookmark").prop('checked', false);
		return;
	</c:if>
	
	<c:if test="${!empty loginMember}">
		memberId = '${loginMember.memberId}';
	</c:if>
	
	let cPage = num;
	const numPerPage = 12;
	let totalPages = 0;
	
	$.ajax({
		url: '${pageContext.request.contextPath}/gathering/searchProBookmark',
		data: {cPage, bookmarkYN,memberId},
		success(bookmarkFilterLists){
			const {bookmarkList, projectList, totalContent, cPage} = bookmarkFilterLists;
 				
				if(bookmarkList == null){
					alert("찜한 프로젝트가 존재하지 않습니다.");
					location.reload();
					return;
				}
				if(projectList == ""){
					document.querySelector(".ps-lists").innerHTML =
						bookmarkList.reduce((html, bookmarkPro, index) => {
							const {psNo, title, viewcount, bookmark, topic, recruited_cnt, people} = bookmarkPro;
							
							return `\${html}
							<div class="ps-pre">
								<a href="<%= request.getContextPath()%>/gathering/projectView?psNo=\${psNo}">
									<img src="<%= request.getContextPath() %>/images/\${topic}.jpg" class="ps-pre__img" alt="해당 프로젝트 주제 이미지">
								</a>
								<p class="bold">\${topic === 'social' ? '소셜네트워크' : (topic === 'game' ? '게임' : (topic === 'travel' ? '여행' : (topic === 'finance' ? '금융' : '이커머스')))}</p>
								<p class="bold ps-title">\${title}</p>
								<ul class="ps-pre__etc">
									<li>
										<span class="heart-emoji">&#9829;</span>\${bookmarkCnt}
									</li>
									<li>모집인원 \${recruited_cnt} / \${people}</li>
								</ul>
								<div class="ps__bookmark">
									<button class='bookmark-back' value='\${psNo}'>♥</button>
									<button style='display:none' class='bookmark-front' value='\${psNo}'>♡</button>
								</div>
							</div>
							`;
						}, "");
				}
				else {
					document.querySelector(".ps-lists").innerHTML =
						projectList.reduce((html, projectListAll, index) => {
							const {psNo, title, viewcount, bookmark, topic, recruited_cnt, people} = projectListAll;
							const bookmarkCnt = bookmark < 0 ? 0 : bookmark;
							
							
							let tagFront = "";
							let tagBack = "";
							outer:
							for(let i = 0; i < bookmarkList.length; i++){
								if(psNo == bookmarkList[i].psNo){
									tagBack = `<button class='bookmark-back' value='\${psNo}'>♥</button>`;
									tagFront = `<button style='display:none' class='bookmark-front' value='\${psNo}'>♡</button>`;
									break outer;
								} else {
									tagBack = `<button style='display:none' class='bookmark-back' value='\${psNo}'>♥</button>`;
									tagFront = `<button class='bookmark-front' value='\${psNo}'>♡</button>`;
								}
							};
							
							return `\${html}
							<div class="ps-pre">
								<a href="<%= request.getContextPath()%>/gathering/projectView?psNo=\${psNo}">
									<img src="<%= request.getContextPath() %>/images/\${topic}.jpg" class="ps-pre__img" alt="해당 프로젝트 주제 이미지">
								</a>
								<p class="bold">\${topic === 'social' ? '소셜네트워크' : (topic === 'game' ? '게임' : (topic === 'travel' ? '여행' : (topic === 'finance' ? '금융' : '이커머스')))}</p>
								<p class="bold ps-title">\${title}</p>
								<ul class="ps-pre__etc">
									<li>
										<span class="heart-emoji">&#9829;</span>\${bookmarkCnt}
									</li>
									<li>모집인원 \${recruited_cnt} / \${people}</li>
								</ul>
								<div class="ps__bookmark">
									\${tagBack}
									\${tagFront}
								</div>
							</div>
							`;
						}, "");
				}
			
			if(totalContent != 0){
				totalPages = Math.ceil(totalContent / numPerPage);
				// pageLink(현재페이지, 전체페이지, 호출할 함수 이름)
				let htmlStr = pageLink(cPage, totalPages, "bookmarkFilter");
				$("#pagebar").html("");
				$("#pagebar").html(htmlStr);
			} else {
				
			}
		},
		error: console.log
	});
}; 

const gatheringFilter = (num) => {
	$("#p__bookmark").prop('checked', false);
	
	const localAll = $("#p__local").val();
	const jobAll = $("#p__job_code").val();
	const statusYN = $("#p__status").is(':checked') ? "N" : "All";
	let memberId = "";
<% if(loginMember != null){ %>
	memberId = '<%= loginMember.getMemberId() %>';
<%
	}
%>
	let cPage = num;
	const numPerPage = 12;
	let totalPages = 0;
	
	let selectLocalKeyword = localAll;
	let selectJobKeyword = jobAll;

	$.ajax({
		url: '<%= request.getContextPath() %>/gathering/searchProFilter',
		data: {
			cPage: cPage,
			selectLocalKeyword: selectLocalKeyword,
			selectJobKeyword: selectJobKeyword,
			statusYN : statusYN,
			memberId: memberId
			},
		success(projectSelectLists){
			const {projectList, totalContent, cPage, bookmarkList} = projectSelectLists;
			
			document.querySelector(".ps-lists").innerHTML =
				// 프로젝트 필터링
				projectList.reduce((html, selectList, index) => {
					const {psNo, title, viewcount, bookmark, topic, recruited_cnt, people} = selectList;
					const bookmarkCnt = bookmark < 0 ? 0 : bookmark;
					let tagFront = "";
					let tagBack = "";
					
					outer:
	 				for(let i = 0; i < bookmarkList.length; i++){
	 					if(psNo == bookmarkList[i].psNo){
	 						tagBack = `<button class='bookmark-back' value='\${psNo}'>♥</button>`;
	 						tagFront = `<button style='display:none' class='bookmark-front' value='\${psNo}'>♡</button>`;
	 						break outer;
	 					} else {
	 						tagBack = `<button style='display:none' class='bookmark-back' value='\${psNo}'>♥</button>`;
	 						tagFront = `<button class='bookmark-front' value='\${psNo}'>♡</button>`;
	 					}
					};
					return `\${html}
					<div class="ps-pre">
						<a href="<%= request.getContextPath()%>/gathering/projectView?psNo=\${psNo}">
							<img src="<%= request.getContextPath() %>/images/\${topic}.jpg" class="ps-pre__img" alt="해당 프로젝트 주제 이미지">
						</a>
						<p class="bold">
						\${topic === 'social' ? '소셜네트워크' : (topic === 'game' ? '게임' : (topic === 'travel' ? '여행' :
							(topic === 'finance' ? '금융' : '이커머스')))}</p>
						<p class="bold ps-title">\${title}</p>
						<ul class="ps-pre__etc">
							<li>
								<span class="heart-emoji">&#9829;</span>\${bookmarkCnt}
							</li>
							<li>모집인원 \${recruited_cnt} / \${people}</li>
						</ul>
						<div class="ps__bookmark">
						<% if(loginMember == null){ %>
							<button class="bookmark-front" onclick="alert('로그인 후 이용해주세요');" value="\${psNo}">♡</button>
						<% } %>
						<% if(loginMember != null){ %>
							\${tagBack}
							\${tagFront}
						<%
						}
						%>
						</div>
					</div>
					`;
				}, '');

			if(totalContent != 0){
				totalPages = Math.ceil(totalContent / numPerPage);
				// pageLink(현재페이지, 전체페이지, 호출할 함수 이름)
				let htmlStr = pageLink(cPage, totalPages, "gatheringFilter");
				$("#pagebar").html("");
				$("#pagebar").html(htmlStr);
			} else {
				$("#pagebar").html("해당되는 프로젝트를 만들어주세요!");
				alert("해당 프로젝트가 존재하지 않습니다.");
			}
		},
		error: console.log
	});
};

function pageLink(cPage, totalPages, funName){
	cPage = Number(cPage);
	totalPages = Number(totalPages);
	let pagebarTag = "";
	const pagebarSize = 5;
	let pagebarStart = (Math.floor((cPage - 1) / pagebarSize) * pagebarSize) + 1;
	let pagebarEnd = pagebarStart + pagebarSize - 1;
	let pageNo = pagebarStart;
	
	// 이전영역
	if(pageNo == 1) {
		
	}
	else {
		pagebarTag += "<a href='javascript:" + funName + "(" + (pageNo - 1) + ");'>이전</a>\n";
	}
	// pageNo영역
	while(pageNo <= pagebarEnd && pageNo <= totalPages) {
		// 현재페이지
		if(pageNo == cPage) {
			pagebarTag += "<span class='cPage'>" + pageNo + "</span>\n";
		}
		// 현재페이지가 아닌 경우
		else {
			pagebarTag += "<a href='javascript:" + funName + "(" + pageNo + ");'>" + pageNo + "</a>\n";
		}
		pageNo++;
	}
	// 다음영역
	if(pageNo > totalPages) {}
	else {
		pagebarTag += "<a href='javascript:" + funName + "(" + pageNo + ")'>다음</a>\n";
	}
	return pagebarTag;
};
<% if(loginMember != null){ %>
$(document).on('click', '.bookmark-front', function(e){
	let mark = e.target;
	const frmAdd = document.addBookmarkFrm;
	let psnum = mark.value;
	mark.style.display = 'none';
	mark.previousElementSibling.style.display = 'block';
	const addBookPs = document.querySelector("#addBookPs");
	addBookPs.value = psnum;
	frmAdd.submit();
});
$(document).on('click', '.bookmark-back', function(e){
	let mark = e.target;
	const frmDel = document.delBookmarkFrm;
	let psnum = mark.value;
	mark.style.display = 'none';
	mark.nextElementSibling.style.display = 'block';
	const delBookPs = document.querySelector("#delBookPs");
	delBookPs.value = psnum;
	frmDel.submit();
});
<% } %>
</script>

	<section class="gathering">
		<!-- 모임페이지 시작 -->
		<!-- 상단 프로젝트/스터디 구분바 -->
		<section class="gathering-bar">
			<p><a href="${pageContext.request.contextPath}/gathering/projectList">프로젝트</a></p>
			<p><a href="${pageContext.request.contextPath}/gathering/studyList">스터디</a></p>
		</section>
		<section class="ps__header">
			<div class="ps__header__text">
				<p> 신규 프로젝트 👨‍💻</p>
			</div>
			<hr>
			<div class="ps__header__content swiper">
				<div class="swiper-wrapper">
				<c:if test="${!empty projectSlideList}">
					<c:forEach items="${projectSlideList}" var="slidePro" varStatus="vs">
					<div class="swiper-slide">
						<a href="${pageContext.request.contextPath}/gathering/projectView?psNo=${slidePro.topic}">
							<img src='<c:url value="/images/${slidePro.topic}.jpg" />' class="ps__header__content__img" alt="해당 프로젝트 주제 이미지">
						</a>
						<ul class="ps__header__content-info">
							<li>
								<p class="bold">
									<c:choose>
										<c:when test="${slidePro.topic eq 'social'}">소셜네트워크</c:when>
										<c:when test="${slidePro.topic eq 'game'}">게임</c:when>
										<c:when test="${slidePro.topic eq 'travel'}">여행</c:when>
										<c:when test="${slidePro.topic eq 'finance'}">금융</c:when>
										<c:when test="${slidePro.topic eq 'ecommerce'}">이커머스</c:when>
									</c:choose>
								</p>
							</li>
							<li><a href="${pageContext.request.contextPath}/gathering/projectView?psNo=${slidePro.psNo}" class="bold">${slidePro.title}</a></li>
							<li class="ps__header__content-content"><p>${slidePro.content}</p></li>
							<li class="bold">
								<span class="heart-emoji">&#9829; ${slidePro.bookmark}</span>
								<span>모집인원 ${slidePro.recruited_cnt} / ${slidePro.people}</span>
							</li>
						</ul>
					</div>
					</c:forEach>
				</c:if>
				</div>
				<div class="swiper-button-next"></div>
				<div class="swiper-button-prev"></div>
				<div class="swiper-pagination"></div>
			</div>
		<hr>
		</section>
		<!-- 프로젝트List -->
		<section class="ps-list-main">
			<h1>전체 프로젝트</h1>
			<div class="ps-filter-container">
				<form name="searchFrm">
					<select name="searchType" value="local" id="p__local" class="ps-filter" onchange="gatheringFilter()">
						<option value="All">지역 미지정</option>
						<option value="Online">온라인</option>
						<option value="Capital">수도권</option>
						<option value="Chungcheong">충청도</option>
						<option value="Gangwon">강원도</option>
						<option value="Jeolla">전라도</option>
						<option value="Gyeongsang">경상도</option>
						<option value="Jeju">제주</option>
					</select>
				</form>
				<form name="searchFrm">
					<select name="searchType" value="jobcode" id="p__job_code" class="ps-filter" onchange="gatheringFilter()">
						<option value="All">직무 미지정</option>
						<option value="PL">기획</option>
						<option value="DG">디자인</option>
						<option value="FE">프론트</option>
						<option value="BE">백엔드</option>
					</select>
				</form>
				<div class="ps-filter">
					<input type="checkbox" id="p__status" name="searchType" onchange="gatheringFilter()">
					<label for="p__status">모집중</label>
				</div>
				<div class="ps-filter">
					<input type="checkbox" id="p__bookmark" name="searchType" onchange="bookmarkFilter()">
					<label for="p__bookmark">찜한 프로젝트</label>
				</div>
				
				<input type="button" class="ps__enroll btn" onclick="projectEnroll();" value="프로젝트 생성">
				<script>
				const projectEnroll = () => {
					if(<%= loginMember == null %>){
						alert('프로젝트 생성은 로그인 후 이용 가능합니다.');	
					}
					else{
						location.href='<%= request.getContextPath()%>/gathering/projectEnrollView';						
					}
				}
				</script>
			</div>
			<div class="ps-lists">
			<c:if test="${!(empty projectList)}">
				<c:forEach items="${projectList}" var="project" varStatus="vs" >
				<div class="ps-pre">
					<a href="${pageContext.request.contextPath}/gathering/projectView?psNo=${project.psNo}">
						<img src="${pageContext.request.contextPath}/images/${project.topic}.jpg" class="ps-pre__img" alt="해당 프로젝트 주제 이미지">
					</a>
					<p class="bold">
							<c:choose>
								<c:when test="${project.topic eq 'social'}">소셜네트워크</c:when>
								<c:when test="${project.topic eq 'game'}">게임</c:when>
								<c:when test="${project.topic eq 'travel'}">여행</c:when>
								<c:when test="${project.topic eq 'finance'}">금융</c:when>
								<c:when test="${project.topic eq 'ecommerce'}">이커머스</c:when>
							</c:choose>
					</p>
					<a href="${pageContext.request.contextPath}/gathering/projectView?psNo=${project.psNo}">
						<p class="bold ps-title">${project.title}</p>
					</a>
					<ul class="ps-pre__etc">
						<li> 
							<span class="heart-emoji">&#9829;</span>${project.bookmark}
						</li>
						<li class="hoverList">
							<span>모집인원 ${project.recruited_cnt} / ${project.people}</span>
						</li>
					</ul>
					<div class="ps__bookmark">
					<c:if test="${empty loginMember}">
						<button "disabled" class="bookmark-front" onclick="alert('로그인 후 이용해주세요');">♡</button>
					</c:if>
					<c:if test="${!empty loginMember}">
						<c:if test="${!empty bookmarkList}">
							<c:set var="loop_flag" value="false"></c:set>
							<c:forEach items="${bookmarkList}" var="bookmark">
								<c:if test="${not loop_flag}">
									<c:if test="${!(project.psNo eq bookmark.psNo)}">
										<c:set var="bstyle">style="display:none"</c:set>
										<c:set var="fstyle">style="display:block"</c:set>
									</c:if>
									<c:if test="${project.psNo eq bookmark.psNo}">
										<%-- 찜을 한 경우 --%>
										<c:set var="bstyle">style="display:block"</c:set>
										<c:set var="fstyle">style="display:none"</c:set>
										<c:set var="loop_flag" value="true"></c:set>
									</c:if>
								</c:if>
							</c:forEach>
						</c:if>
						<button ${bstyle} class='bookmark-back' value="${project.psNo}">♥</button>
						<button ${fstyle} class='bookmark-front' value="${project.psNo}">♡</button>
					</c:if>
					</div>
				</div>
				</c:forEach>
			</c:if>
			</div>
			<div id="pagebar">
				${pagebar}
			</div>
			<c:if test="${!empty loginMember}">
			<form
				action="${pageContext.request.contextPath}/membus/bookmarkAdd" id="tt" method="POST" name="addBookmarkFrm">
				<input type="hidden" name="psNo" id="addBookPs"/>
				<input type="hidden" name="member_id" value="${loginMember.memberId}" />
			</form>
			<form
				action="${pageContext.request.contextPath}/membus/bookmarkDel" method="POST" name="delBookmarkFrm">
				<input type="hidden" name="psNo" id="delBookPs"/>
				<input type="hidden" name="member_id" value="${loginMember.memberId}" />
			</form>
			</c:if>
		</section>
	</section>
<script>

<% if(loginMember != null){ %>
document.querySelectorAll(".ps__bookmark").forEach((bookmark) => {
	bookmark.addEventListener('click', (e) => {
		let mark = e.target;
		const frmAdd = document.addBookmarkFrm;
		const frmDel = document.delBookmarkFrm;
		let psnum = mark.value;

		if(mark.classList.contains("bookmark-front")) {
			mark.style.display = 'none';
			mark.previousElementSibling.style.display = 'block';
			const addBookPs = document.querySelector("#addBookPs");
			addBookPs.value = psnum;
			frmAdd.submit();			
		} else {
			mark.style.display = 'none';
			mark.nextElementSibling.style.display = 'block';
			const delBookPs = document.querySelector("#delBookPs");
			delBookPs.value = psnum;
			frmDel.submit();
		}
	})
});
<% } %>
</script>	
	
<%@ include file="/WEB-INF/views/common/footer.jsp" %>