package simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.Arrays;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class CrudSimulations extends Simulation {

	// HTTP 프로토콜 설정
	HttpProtocolBuilder httpProtocol = http
		.baseUrl("http://localhost:10000")
		.acceptHeader("application/json");

	// 1) 쓰기(Write) 시나리오: 로그인 → 페이지 로드 → 이미지 업로드 → 게시글 등록 → 최종 이미지 처리 → 수정(파일 포함)
	ScenarioBuilder writeScn = scenario("Write Scenario with Full Summernote Flow")
		.feed(csv("accounts.csv").circular())

		// 로그인
		.exec(http("User Login")
			.post("/user/loginProc")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.formParam("username", "#{username}")
			.formParam("password", "#{password}")
			.check(status().is(200))
			.check(header("Set-Cookie").find().saveAs("rawCookies"))
		)
		.pause(Duration.ofMillis(100))

		// JWT 추출
		.exec(session -> {
			String raw = session.getString("rawCookies");
			String jwt = Arrays.stream(raw.split(";"))
				.map(String::trim)
				.filter(s -> s.startsWith("access="))
				.map(s -> s.substring("access=".length()))
				.findFirst().orElse("");
			return session.set("jwt", jwt);
		})
		.pause(Duration.ofMillis(100))

		// 페이지 로드 (GET /board/register)
		.exec(http("Load Register Page")
			.get("/board/register")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.check(status().is(200))
		)
		.pause(Duration.ofSeconds(1))

		// 2) Summernote 이미지 업로드 (비동기)
		.exec(http("Upload Summernote Image")
			.post("/api/upload/summernote-upload")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.bodyPart(RawFileBodyPart("files", "img/삽입 이미지.PNG")
				.fileName("삽입 이미지.PNG")
				.contentType("image/png")
			)
			.asMultipartForm()
			.check(status().is(200))
			.check(jsonPath("$.files[0].imageUrl").saveAs("imageUrl"))
			.check(jsonPath("$.files[0].uuid").saveAs("imageUuid"))
		)
		.pause(Duration.ofSeconds(1))

		// 3) 게시글 등록 (본문에 <img> 태그 포함)
		.exec(session -> {
			String url = session.getString("imageUrl");
			String uuid = session.getString("imageUuid");
			String imgTag = String.format(
				"<img src=\"%s\" data-uuid=\"%s\" data-filename=\"삽입 이미지.PNG\" data-extension=\"png\" style=\"width:75%%;\"/>",
				url, uuid
			);
			String content = "<p>자동 삽입된 이미지:</p>" + imgTag;
			return session
				.set("postTitle", "Summernote 전체 플로우 테스트")
				.set("postContent", content);
		})
		.exec(http("Create Post with Embedded Image")
			.post("/board/register")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.formParam("boardCode", "general")
			.formParam("postTitle", session -> session.getString("postTitle"))
			.formParam("postContent", session -> session.getString("postContent"))
			.check(status().is(200), jsonPath("$.postNo").saveAs("postNo"))
		)
		// 4) 최종 이미지 처리 (uploadFinalImage)
		.exec(http("Final Image Upload")
			.post("/api/upload/finalImageUpload")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.bodyPart(RawFileBodyPart("files", "img/삽입 이미지.PNG")
				.fileName("삽입 이미지.PNG")
				.contentType("image/png")
			)
			.bodyPart(StringBodyPart("postNo", session -> session.getString("postNo")))
			.bodyPart(StringBodyPart("uuids", session -> session.getString("imageUuid")))
			.asMultipartForm()
			.check(status().is(200))
		)
		.pause(Duration.ofSeconds(3))

		.exec(http("Upload Edit Image")
			.post("/api/upload/summernote-upload")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.bodyPart(RawFileBodyPart("files", "img/수정 이미지.png")
				.fileName("수정 이미지.png")
				.contentType("image/png")
			)
			.asMultipartForm()
			.check(status().is(200))
			.check(jsonPath("$.files[0].imageUrl").saveAs("editImageUrl"))
			.check(jsonPath("$.files[0].uuid").saveAs("editImageUuid"))
		)
		.pause(Duration.ofSeconds(1))
		.exec(session -> {
			String url = session.getString("editImageUrl");
			String uuid = session.getString("editImageUuid");
			String imgTag = String.format(
				"<img src=\"%s\" data-uuid=\"%s\" data-filename=\"수정 이미지.png\" data-extension=\"png\" style=\"width:75%%;\"/>",
				url, uuid
			);
			String content = "<p>수정된 이미지 삽입:</p>" + imgTag;
			return session.set("editContent", content);
		})
		.exec(http("Modify Post with Embedded Image")
			.post("/board/modify")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.formParam("postNo", session -> session.getString("postNo"))
			.formParam("userId", "#{username}")
			.formParam("boardCode", "general")
			.formParam("postTitle", "수정 이미지 테스트")
			.formParam("postContent", session -> session.getString("editContent"))
			.check(status().is(200))
		)
		.pause(Duration.ofSeconds(3))
		.exec(http("Final Edit Image Upload")
			.post("/api/upload/finalImageUpload")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.bodyPart(RawFileBodyPart("files", "img/수정 이미지.png")
				.fileName("수정 이미지.png")
				.contentType("image/png")
			)
			.bodyPart(StringBodyPart("postNo", session -> session.getString("postNo")))
			.bodyPart(StringBodyPart("uuids", session -> session.getString("editImageUuid")))
			.asMultipartForm()
			.check(status().is(200))
		)
		.pause(Duration.ofMillis(50))

		// // 게시글 삭제
		.exec(http("DeletePost")
			.get(session -> "/board/delete/" + session.getString("postNo"))
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.check(status().is(200))
		);

	// 2) 읽기(Read) 시나리오: 로그인 → 목록 조회 → 상세 조회
	ScenarioBuilder readScn = scenario("Read Scenario")
		.feed(csv("accounts.csv").circular())
		// 로그인
		.exec(http("User Login")
			.post("/user/loginProc")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.formParam("username", "#{username}")
			.formParam("password", "#{password}")
			.check(status().is(200))
			.check(header("Set-Cookie").find().saveAs("rawCookies"))
		)
		.exec(session -> {
			String raw = session.getString("rawCookies");
			String jwt = Arrays.stream(raw.split(";"))
				.map(String::trim)
				.filter(s -> s.startsWith("access="))
				.map(s -> s.substring("access=".length()))
				.findFirst().orElse("");
			return session.set("jwt", jwt);
		})
		.pause(Duration.ofMillis(20))
		// 게시판 목록 조회
		.exec(http("ListPosts")
			.get("/board/general")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.check(status().is(200))
		)
		.pause(Duration.ofMillis(20))
		// 게시글 상세 조회
		.exec(http("GetPostDetail")
			.get("/board/read/5")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.check(status().is(200))
		);

	// 시나리오 병렬 실행
	// {
	//  setUp(
	//      writeScn.injectOpen(
	//          atOnceUsers(1)
	//      ),
	//      readScn.injectOpen(
	//          atOnceUsers(1)
	//      )
	//  ).protocols(httpProtocol);
	// }

	{
		setUp(
			// 쓰기 시나리오: 초당 10명, 4분간 지속
			writeScn.injectOpen(
				constantUsersPerSec(10).during(Duration.ofMinutes(4)).randomized()
			),
			// 읽기 시나리오: 초당 20명, 4분간 지속
			readScn.injectOpen(
				constantUsersPerSec(20).during(Duration.ofMinutes(4)).randomized()
			)
		).protocols(httpProtocol);
	}
}
