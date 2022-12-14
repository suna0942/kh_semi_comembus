package kh.semi.comembus.gathering.model.dao;
import static kh.semi.comembus.common.JdbcTemplate.close;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kh.semi.comembus.gathering.model.dto.Gathering;
import kh.semi.comembus.gathering.model.dto.GatheringExt;
import kh.semi.comembus.gathering.model.dto.GatheringType;
import kh.semi.comembus.gathering.model.dto.Status;
import kh.semi.comembus.gathering.model.exception.GatheringException;
import kh.semi.comembus.member.model.dto.JobCode;
import kh.semi.comembus.member.model.dto.MemberApplicationStatus;

public class GatheringDao {
	private Properties prop = new Properties();
	
	public GatheringDao() {
		String filename = GatheringDao.class.getResource("/sql/gathering/gathering-query.properties").getPath();
		try {
			prop.load(new FileReader(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Gathering> findGatheringAll(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> projectList = new ArrayList<>();
		String type = (String) param.get("type");
		String sql = "";
		if(type == "P") {
			sql = prop.getProperty("findProjectAll");
		} else {
			sql = prop.getProperty("findStudyAll");
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("start"));
			pstmt.setInt(2, (int)param.get("end"));
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				projectList.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 목록 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return projectList;
	}
	

	public List<Gathering> findProjectSlide(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> projectSlideList = new ArrayList<>();
		String sql = sql = prop.getProperty("findProjectSlide");
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				projectSlideList.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("슬라이드용 목록 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return projectSlideList;
	}
	
	public List<Gathering> findStudySlide(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> studySlideList = new ArrayList<>();
		String sql = sql = prop.getProperty("findStudySlide");
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				studySlideList.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("슬라이드용 목록 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return studySlideList;
	}
	private GatheringExt handleGatheringResultSet(ResultSet rset) throws SQLException {
		int psNo = rset.getInt("ps_no");
		String writer = rset.getString("writer");
		GatheringType psType = GatheringType.valueOf(rset.getString("gathering_type"));
		String title = rset.getString("title");
		Date regDate = rset.getDate("reg_date");
		String content = rset.getString("content");
		int viewcount = rset.getInt("viewcount");
		int bookmark = rset.getInt("bookmark");
		String topic = rset.getString("topic");
		String local = rset.getString("local");
		int people = rset.getInt("people");
		Status status = Status.valueOf(rset.getString("status"));
		Date startDate = rset.getDate("start_date");
		Date endDate = rset.getDate("end_date");
		return new GatheringExt(psNo, writer, psType, title, regDate, content, viewcount, bookmark, topic, local, people, status, startDate, endDate);
	}

	public int getProTotalContent(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int totalContent = 0;
		String sql = prop.getProperty("getProjectTotalContent");
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			if(rset.next())
				totalContent = rset.getInt(1);
		} catch (SQLException e) {
			throw new GatheringException("총 프로젝트 게시물 수 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}		
		return totalContent;
	}
	
	public int getStdTotalContent(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int totalContent = 0;
		String sql = prop.getProperty("getStdTotalContent");
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			if(rset.next())
				totalContent = rset.getInt(1);
		} catch (SQLException e) {
			throw new GatheringException("총 스터디 게시물 수 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}		
		return totalContent;
	}


	public List<Gathering> findProjectLike(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> list = new ArrayList<>();
		String sql = prop.getProperty("findProjectLike");
		
		String selectLocalKeyword = (String) param.get("selectLocalKeyword");
		String selectJobKeyword = (String) param.get("selectJobKeyword");
		String statusYN = (String) param.get("statusYN");
		int start = (int) param.get("start");
		int end = (int) param.get("end");

		// select * from (
			// select row_number() over(order by reg_date desc) rnum, ps.*,
			//(select nvl(sum(recruited_number), 0) from project_member_dept where ps_no = ps.ps_no) recruited_cnt
			// from project_study ps where gathering_type = 'P'
			// [str1] [str2] [str3] and end_date > current_date) p 
			// where rnum between ? and ?

		// 랜딩페이지(필터 미지정시)
		if("All".equals(selectLocalKeyword)) {
			if("All".equals(selectJobKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		else {
			if("All".equals(selectJobKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, start);
			pstmt.setInt(2, end);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				list.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return list;
	}

	public int getProTotalContentLike(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int totalContent = 0;		
		String sql = prop.getProperty("getProTotalContentLike");
		
		String selectLocalKeyword = (String) param.get("selectLocalKeyword");
		String selectJobKeyword = (String) param.get("selectJobKeyword");
		String statusYN = (String) param.get("statusYN");
		
		// 랜딩페이지(필터 미지정시)
		if("All".equals(selectLocalKeyword)) {
			if("All".equals(selectJobKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		else {
			if("All".equals(selectJobKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and ps_no in(select ps_no from project_member_dept where ps_no = ps.ps_no and job_code in('" + selectJobKeyword + "') and capacity_number > recruited_number)");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			if(rset.next())
				totalContent = rset.getInt(1);
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return totalContent;
	}
	
	public List<Gathering> findStudyLike(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> list = new ArrayList<>();
		String sql = prop.getProperty("findStudyLike");
		
		String selectLocalKeyword = (String) param.get("selectLocalKeyword");
		String selectTopicKeyword = (String) param.get("selectTopicKeyword");
		String statusYN = (String) param.get("statusYN");
		int start = (int) param.get("start");
		int end = (int) param.get("end");

		// [str1] = "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))"
        // [str2] = "and topic in ('" + selectTopicKeyword + "')"
		// [str3] = "and status = '" + statusYN + "'"

		// 랜딩페이지(필터 미지정시)
		if("All".equals(selectLocalKeyword)) {
			if("All".equals(selectTopicKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		else {
			if("All".equals(selectTopicKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, start);
			pstmt.setInt(2, end);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				list.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("스터디 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return list;
	}

	public int getStdTotalContentLike(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int totalContent = 0;		
		String sql = prop.getProperty("getStdTotalContentLike");
		
		String selectLocalKeyword = (String) param.get("selectLocalKeyword");
		String selectTopicKeyword = (String) param.get("selectTopicKeyword");
		String statusYN = (String) param.get("statusYN");
		
		// 랜딩페이지(필터 미지정시)
		if("All".equals(selectLocalKeyword)) {
			if("All".equals(selectTopicKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", " ");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		else {
			if("All".equals(selectTopicKeyword)) {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", " ");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			} else {
				if("All".equals(statusYN)) {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", " ");
				} else {
					sql = sql.replace("[str1]", "and ps_no in(select ps_no from project_study where ps_no = ps.ps_no and upper(local) = upper('" + selectLocalKeyword + "'))");
					sql = sql.replace("[str2]", "and topic in ('" + selectTopicKeyword + "')");
					sql = sql.replace("[str3]", "and status = '" + statusYN + "'");
				}
			}
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			if(rset.next())
				totalContent = rset.getInt(1);
		} catch (SQLException e) {
			throw new GatheringException("스터디 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return totalContent;
	}
	
	// pagebar 필요한 찜 필터용
	public List<Gathering> findProBookmarkFilter(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> bookmarkFilterlist = new ArrayList<>();
		String sql = prop.getProperty("findProBookmarkFilter");
		
		String bookmarkYN = (String) param.get("bookmarkYN");
		String memberId = (String) param.get("memberId");
		// [str1] = "and exists (select 1 from bookmarked_prj_std where ps_no = ps.ps_no and member_id = '" + memberId + "')"

		// 체크 시
		if("Y".equals(bookmarkYN)) {
			sql = sql.replace("[str1]", "and exists (select 1 from bookmarked_prj_std where ps_no = ps.ps_no and member_id = '" + memberId + "')");			
		} else {
			sql = sql.replace("[str1]", " ");
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("start"));
			pstmt.setInt(2, (int)param.get("end"));
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				bookmarkFilterlist.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 찜 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return bookmarkFilterlist;
	}

	public int getTotalBookmarkFilter(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int totalbookmarkFilterContent = 0;
		String bookmarkYN = (String) param.get("bookmarkYN");
		String memberId = (String) param.get("memberId");
		String type = (String) param.get("type");
		String sql = "";
		
		if(type == "P") {
			sql = prop.getProperty("getTotalProBookmarkFilter");
		} else {
			sql = prop.getProperty("getTotalStdBookmarkFilter");
		}

		if("Y".equals(bookmarkYN)) {
			sql = sql.replace("[str1]", "and exists (select 1 from bookmarked_prj_std where ps_no = ps.ps_no and member_id = '" + memberId + "')");			
		} else {
			sql = sql.replace("[str1]", " ");
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			if(rset.next()) {
				totalbookmarkFilterContent = rset.getInt(1);
			}
		} catch (SQLException e) {
			throw new GatheringException("스터디 찜 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return totalbookmarkFilterContent;
	}
	
	// pagebar 필요한 찜 필터용
	public List<Gathering> findStdBookmarkFilter(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		List<Gathering> bookmarkFilterlist = new ArrayList<>();
		String sql = prop.getProperty("findStdBookmarkFilter");
		
		String bookmarkYN = (String) param.get("bookmarkYN");
		String memberId = (String) param.get("memberId");

		// 체크 시
		if("Y".equals(bookmarkYN)) {
			sql = sql.replace("[str1]", "and exists (select 1 from bookmarked_prj_std where ps_no = ps.ps_no and member_id = '" + memberId + "')");			
		} else {
			sql = sql.replace("[str1]", " ");
		}
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("start"));
			pstmt.setInt(2, (int)param.get("end"));
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				bookmarkFilterlist.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("스터디 찜 필터 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}
		return bookmarkFilterlist;
	}
	
	public List<Gathering> findAllProBookmarked(Connection conn, Map<String, Object> bmParam) {
		PreparedStatement pstmt = null;
		List<Gathering> gatheringBookmarkList = new ArrayList<>();
		ResultSet rset = null;
		String sql = prop.getProperty("findAllProBookmarked");
		String loginMemberId = (String) bmParam.get("loginMemberId");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginMemberId);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				gatheringBookmarkList.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("찜하기 모임 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		return gatheringBookmarkList;
	}
	
	public List<Gathering> findAllStdBookmarked(Connection conn, Map<String, Object> bmParam) {
		PreparedStatement pstmt = null;
		List<Gathering> gatheringBookmarkList = new ArrayList<>();
		ResultSet rset = null;
		String sql = prop.getProperty("findAllStdBookmarked");
		String loginMemberId = (String) bmParam.get("loginMemberId");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loginMemberId);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				GatheringExt gathering = handleGatheringResultSet(rset);
				gathering.setRecruited_cnt(rset.getInt("recruited_cnt"));
				gatheringBookmarkList.add(gathering);
			}
		} catch (SQLException e) {
			throw new GatheringException("찜하기 모임 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		return gatheringBookmarkList;
	}
	
	// 북마크 추가 시 project_study 테이블 update
	public int addBookmark(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("updateBmk");
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int) param.get("psNo"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("bookmark 증가 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}
	
	// 북마크 삭제 시 project_study 테이블 update	
	public int delBookmark(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("deleteBmk");
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int) param.get("psNo"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("bookmark 감소 오류", e);
		} finally {
			close(pstmt);
		}
		return result;
	}
	
	// 선아 코드 끝
	
	//수진코드 시작
	/**
	 * 멤버스 프로필,마이페이지: 회원이 참가한 모임 게시글 조회
	 */
	public List<GatheringExt> findAllByMemberId(Connection conn, String memberId) {
		List<GatheringExt> gatheringIngList = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("findAllByMemberId");
		//1:memberId
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, memberId);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gather = handleGatheringResultSet(rset);
				gatheringIngList.add(gather);
			}
		} catch (SQLException e) {
			throw new GatheringException("참여중 모임 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		
		return gatheringIngList;
	}
	
	/**
	 * 멤버스 프로필,마이페이지: 찜하기 한 모임게시글목록 조회
	 */
	public List<GatheringExt> findAllBookmarked(Connection conn, String memberId) {
		List<GatheringExt> gatheringBookmarkList = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("findAllBookmarked");
		//1:memberId
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, memberId);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gather = handleGatheringResultSet(rset);
				gather.setRecruited_cnt(0);
				gatheringBookmarkList.add(gather);
			}
		} catch (SQLException e) {
			throw new GatheringException("찜하기 모임 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		return gatheringBookmarkList;
	}
	
	/**
	 * 멤버스마이페이지: 회원별 지원한 모임게시글목록조회
	 */
	public List<GatheringExt> findAllApldByMemberId(Connection conn, String memberId) {
		List<GatheringExt> gatheringApldList = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("findAllApldByMemberId");
		//1:memberId
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, memberId);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				GatheringExt gather = handleGatheringResultSet(rset);
				gatheringApldList.add(gather);
			}
		} catch (SQLException e) {
			throw new GatheringException("모임 지원현황 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		
		return gatheringApldList;
	}
	
	/**
	 * 멤버스마이페이지: 모임 지원 취소하기
	 */
	public int cancelApld(Connection conn, Map<String, Object> param) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("cancelApld");
		//update member_application_status set result = 'X' where member_id = ? and ps_no = ? and result='W'
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, (String)param.get("memberId"));
			pstmt.setInt(2, (int)param.get("psNo"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("모임 지원 취소 오류", e);
		} finally {
			close(pstmt);
		}	
		
		return result;
	}
	
	/**
	 * 모임 게시글 번호로 조회하기
	 * - 지원신청 취소시 해당 게시글 정보 확인을 위해 작성했습니다. 
	 */
	public GatheringExt findByNo(Connection conn, int psNo) {
		GatheringExt gather = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("findByNo");
		//1: psNo
		System.out.println("dao1 : "+psNo);

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				gather = handleGatheringResultSet(rset);
			}
		} catch (SQLException e) {
			throw new GatheringException("모임 게시글번호 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		System.out.println("dao2 : "+psNo);

		return gather;
	}
	
	/**
	 * 모임게시글상세>지원자현황페이지(프로젝트): 직무별 모집정원 조회 
	 */
	public Map<String, Integer> getCapacitiesByDept(Connection conn, int psNo) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		Map<String, Integer> capacitiesByDept = new HashMap<>();
		String sql = prop.getProperty("getCapacitiesByDept");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				JobCode jobCode = JobCode.valueOf(rset.getString("job_code"));
				int capa = rset.getInt("capacity_number");
				switch(jobCode) {
				case PL: capacitiesByDept.put("PL", capa); break;
				case DG: capacitiesByDept.put("DG", capa); break;
				case BE: capacitiesByDept.put("BE", capa); break;
				case FE: capacitiesByDept.put("FE", capa); break;
				}
			}
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 직무별 정원 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		
		return capacitiesByDept;
	}
	
	public Map<String, Integer> getCntsByDept(Connection conn, int psNo) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		Map<String, Integer> cntsByDept = new HashMap<>();
		String sql = prop.getProperty("getCntsByDept");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				JobCode jobCode = JobCode.valueOf(rset.getString("job_code"));
				int capa = rset.getInt("recruited_number");
				switch(jobCode) {
				case PL: cntsByDept.put("PL", capa); break;
				case DG: cntsByDept.put("DG", capa); break;
				case BE: cntsByDept.put("BE", capa); break;
				case FE: cntsByDept.put("FE", capa); break;
				}
			}
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 직무별 모집인원 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		return cntsByDept;
	}
	
	/**
	 * 모임게시글상세>지원자현황페이지: 직무별 모집인원 테이블 업데이트
	 */
	public int addPSMemNumByDept(Connection conn, Map<String, Object> param) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("updatePSMemNumByDept");
		try {//1:psNo, 2: jobCode
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("psNo"));
			pstmt.setString(2, (String) param.get("jobCode"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 직무별 모집된 인원수 업데이트 오류", e);
		} finally {
			close(pstmt);
		}	
		return result;
	}
	
	/**
	 * 멤버별 모임지원현황 결과 컬럼 업데이트
	 */
	public int updateApldResult(Connection conn, Map<String, Object> param) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("updateApldResult");
		try {//1:psNo, 2:
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, (String) param.get("apldResult"));
			pstmt.setString(2, (String) param.get("apldMemberId"));
			pstmt.setInt(3, (int)param.get("psNo"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("프로젝트 직무별 모집된 인원수 업데이트 오류", e);
		} finally {
			close(pstmt);
		}	
		return result;
	}
	
	/**
	 * 스터디 지원자 합격자 추가
	 */
	public int addStdMemNum(Connection conn, int psNo) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("addStdMemNum");
		try {//1:psNo, 
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("스터디 모집된 인원수 업데이트 오류", e);
		} finally {
			close(pstmt);
		}	
		return result;
	}
	
	public int getRcrtdForStd(Connection conn, int psNo) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int capa = 0;
		String sql = prop.getProperty("getRcrtdForStd");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				capa = rset.getInt(1);
			}
		} catch (SQLException e) {
			throw new GatheringException("스터디 모집된인원 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		return capa;
	}
	
	public int attachRctdCntToGather(Connection conn, int psNo) {
		int rctdCnt = 0;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		String sql = prop.getProperty("attachRctdCntToGather");
		try {//
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			rset = pstmt.executeQuery();
			while(rset.next()) {
				rctdCnt = rset.getInt(1);
			}
		} catch (SQLException e) {
			throw new GatheringException("총 모집된 인원 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
				
		return rctdCnt;
	}
	
	public int enrollDeptCnt(Connection conn, Map<String, Object> param) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("enrollProjectDept");
		String jobCode = (String)param.get("jobCode");
		int cnt = (int)param.get("cnt");
		try {//psNo jobCode cnt
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("psNo"));
			pstmt.setString(2, jobCode);
			pstmt.setInt(3, cnt);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("직무별 정원 등록 오류", e);
		} finally {
			close(pstmt);
		}			
		
		return result;
	}

	public int updateNumByDept(Connection conn, Map<String, Object> param) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("updateNumByDept");
		try {//update project_member_dept set capacity_number = ? where ps_no = ? and job_code = ?
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("num"));
			pstmt.setInt(2, (int) param.get("psNo"));
			pstmt.setString(3, (String) param.get("jobCode"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("직무별 정원 변경 오류", e);
		} finally {
			close(pstmt);
		}			
		
		return result;
	}
	

	public int isExistRow(Connection conn, Map<String, Object> param) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int isExist = 0;
		String sql = prop.getProperty("isExistRow");
		try {//select count(*)s from project_member_dept where ps_no = ? and job_code = ?
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("psNo"));
			pstmt.setString(2, (String) param.get("jobCode"));
			rset = pstmt.executeQuery();
			while(rset.next()) {
				isExist = rset.getInt(1);
			}
		} catch (SQLException e) {
			throw new GatheringException("직무별 정원수 조회 오류", e);
		} finally {
			close(rset);
			close(pstmt);
		}	
		return isExist;
	}
	
	public int insertZeroToDept(Connection conn, Map<String, Object> param) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("insertZeroToDept");
		try {//insert into project_member_dept values (seq_p_m_dept_no.nextval, psNo?, jobCode?, 0, default)
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, (int)param.get("psNo"));
			pstmt.setString(2, (String)param.get("jobCode"));
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("직무별 정원수 초기화 오류", e);
		} finally {
			close(pstmt);
		}	
		
		return result;
	}
	
	//수진코드 끝
	
	// 유경 코드 시작
	public int enrollProject(Connection conn, Gathering project) {
		PreparedStatement pstmt=null;
		int result = 0;
		String sql=prop.getProperty("insertProject");
		try {
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, project.getWriter());
//			pstmt = setString(2, project.getPsType() != null ? project.getPsType().name() : null);
			pstmt.setString(2, project.getTitle());
//			pstmt.setDate(3, project.getRegDate());
			pstmt.setString(3, project.getContent());
//			pstmt.setInt(6, project.getViewcount());
//			pstmt.setInt(7, project.getBookmark());
			pstmt.setString(4, project.getTopic());
			pstmt.setString(5, project.getLocal());
			pstmt.setInt(6, project.getPeople());//people추후 설정하기
//			pstmt.setString(8, project.getStatus().name());
			pstmt.setDate(7, project.getStartDate());
			pstmt.setDate(8, project.getEndDate());
			
			result=pstmt.executeUpdate();
		}catch(SQLException e) {
			throw new GatheringException("프로젝트 등록 오류",e);
		}finally {
			close(pstmt);
		}
		return result;
	}

	public int getLastProjectNo(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int projectNo=0;
		//sql설정
		String sql = prop.getProperty("getLastProjectNo");
		try {
			pstmt=conn.prepareStatement(sql);
			rset=pstmt.executeQuery();
			if(rset.next()) {
				projectNo=rset.getInt(1);
			}
		}catch(SQLException e) {
			throw new GatheringException("생성된 프로젝트번호 조회 오류",e);
		}finally {
			close(rset);
			close(pstmt);
		}
		return projectNo;
	}

	public int updateReadCount(Connection conn, int psNo) {
		int result=0;
		return result;
	}

	public int enrollStudy(Connection conn, Gathering study) {
		PreparedStatement pstmt=null;
		int result = 0;
		String sql=prop.getProperty("insertStudy");
		try {
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, study.getWriter());
//			pstmt.setObject(2, "S");
			pstmt.setString(2, study.getTitle());
//			pstmt.setDate(4, study.getRegDate());
			pstmt.setString(3, study.getContent());
//			pstmt.setInt(6, study.getViewcount());
//			pstmt.setInt(7, study.getBookmark());
			pstmt.setString(4, study.getTopic());
			pstmt.setString(5, study.getLocal());
			pstmt.setInt(6, study.getPeople());
//			pstmt.setObject(11, "N");
			pstmt.setDate(7, study.getStartDate());
			pstmt.setDate(8, study.getEndDate());
			
			result=pstmt.executeUpdate();
		}catch(SQLException e) {
			throw new GatheringException("스터디 등록 오류",e);
		}finally {
			close(pstmt);
		}
		return result;
	}

	public int getLastStudyNo(Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int studyNo=0;
		//sql설정
		String sql = prop.getProperty("getLastStudyNo");
		try {
			pstmt=conn.prepareStatement(sql);
			rset=pstmt.executeQuery();
			if(rset.next()) {
				studyNo=rset.getInt(1);
			}
		}catch(SQLException e) {
			throw new GatheringException("생성된 스터디 번호 조회 오류",e);
		}finally {
			close(rset);
			close(pstmt);
		}
		return studyNo;
	}

	public int enrollProjectDep(Connection conn, GatheringExt projectDep) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("enrollProjectDep");
		//psNo dept cnt		
		try {
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new GatheringException("직무 등록 오류!", e);
		} finally {
			close(pstmt);
		}	
		return result;
	}

	public int updateProject(Connection conn, Gathering project) {
		PreparedStatement pstmt=null;
		int result = 0;
		String sql=prop.getProperty("updateProject");
		try {
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, project.getTitle());
			pstmt.setString(2, project.getContent());
			pstmt.setInt(3, project.getPeople());
			pstmt.setString(4, project.getLocal());
			pstmt.setDate(5, project.getStartDate());
			pstmt.setDate(6, project.getEndDate());
			pstmt.setInt(7, project.getPsNo());
			result=pstmt.executeUpdate();
		}catch(SQLException e) {
			throw new GatheringException("프로젝트 수정 오류",e);
		}finally {
			close(pstmt);
		}
		return result;
	}


	public int deleteProject(Connection conn, int psNo) {
		PreparedStatement pstmt = null;
		int result = 0;
		String sql = prop.getProperty("deleteProject");
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, psNo);
			result = pstmt.executeUpdate();
		} 
		catch (SQLException e) {
			throw new GatheringException("프로젝트 삭제 오류!", e);
		}
		finally {
			close(pstmt);
		}
		return result;
	}
	
	// 유경 코드 끝
	
	// 미송 코드 시작
	public int updateStudy(Connection conn, Gathering study) {
		PreparedStatement pstmt=null; 
		int result = 0;
		String sql=prop.getProperty("updateStudy");
		try {
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, study.getTitle());
			pstmt.setString(2, study.getContent());
			pstmt.setInt(3, study.getPeople());
			pstmt.setString(4, study.getLocal());
			pstmt.setDate(5, study.getStartDate());
			pstmt.setDate(6, study.getEndDate());
			pstmt.setInt(7, study.getPsNo());
			result=pstmt.executeUpdate();
		}catch(SQLException e) {
			throw new GatheringException("스터디 수정 오류",e);
		}finally {
			close(pstmt);
		}
		return result;
	}
	
	public int insertMemberApply(Connection conn, MemberApplicationStatus applyInfo) {
		PreparedStatement pstmt=null; 
		int result = 0;
		String sql=prop.getProperty("insertMemberApply");
		try {
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, applyInfo.getMemberId());
			pstmt.setInt(2, applyInfo.getPsNo());
			result=pstmt.executeUpdate();
		}catch(SQLException e) {
			throw new GatheringException("모임 지원하기 오류",e);
		}finally {
			close(pstmt);
		}
		return result;
	}
	// 미송 코드 끝


}
