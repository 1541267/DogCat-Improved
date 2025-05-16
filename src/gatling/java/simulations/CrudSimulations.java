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
		.acceptHeader("application/json")
		.contentTypeHeader("application/json");

	// 시나리오 정의
	ScenarioBuilder scn = scenario("말랑발자국 CRUD 부하 테스트")
		//feeder 더미 계정들
		.feed(csv("accounts.csv").circular())
		// 1) 로그인
		.exec(http("User Login")
			.post("/user/loginProc")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.formParam("username", "#{username}")
			.formParam("password", "#{password}")
			.formParam("autoLogin", "false")
			.formParam("rememberMe", "false")
			.check(status().is(200))
			.check(header("Set-Cookie").find().saveAs("rawCookies"))
		)

		// 2) JWT만 추출해서 세션에 저장
		.exec(session -> {
			String raw = session.getString("rawCookies");
			String jwt = Arrays.stream(raw.split(";"))
				.map(String::trim)
				.filter(s -> s.startsWith("access="))
				.map(s -> s.substring("access=".length()))
				.findFirst()
				.orElse("");
			return session.set("jwt", jwt);
		})

		// 3) 인증된 상태에서 메인 페이지 호출
		.exec(http("After Login")
			.get("/")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.check(status().is(200))
		)
		.pause(Duration.ofMillis(100))

		// 4) 게시글 생성
		.exec(http("CreatePost")
			.post("/board/register/")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.formParam("boardCode", "general")
			.formParam("postTitle", "성능테스트")
			.formParam("postContent", "x".repeat(1000))
			.check(status().is(200), jsonPath("$.postNo").saveAs("postNo"))
		)
		.pause(Duration.ofMillis(100))

		// 5) 게시글 수정
		.exec(http("UpdatePost")
			.post("/board/modify")
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.formParam("postNo", session -> session.getString("postNo"))
			.formParam("userId", "#{username}")
			.formParam("boardCode", "general")
			.formParam("postTitle", "수정 성능 테스트")
			.formParam("postContent", "x".repeat(1000))
			.check(status().is(200))
		)

		// 6) 삭제
		.exec(http("DeletePost")
			.get(session -> "/board/delete/" + session.getString("postNo"))
			.header("Cookie", session -> "access=" + session.getString("jwt"))
			.check(status().is(200))
		);

	{
		// 가상 유저 1명으로 테스트 실행
		// setUp(
		// 	scn.injectOpen(atOnceUsers(1))
		// ).protocols(httpProtocol);

		// setUp(
		// 	scn.injectOpen(
		// 		// 1분 동안 0 → 500명으로 램핑
		// 		rampUsers(500).during(Duration.ofMinutes(1)),
		// 		// 그 뒤 4분간 초당 평균 20명 수준으로 유지 (랜덤 분산)
		// 		constantUsersPerSec(20).during(Duration.ofMinutes(3)).randomized(),
		// 		// 마지막으로 30초 동안 급격히 1000명까지 올리고
		// 		rampUsers(900).during(Duration.ofSeconds(30))
		// 	)
		// ).protocols(httpProtocol);

		{
			setUp(
				scn.injectOpen(
					// 1분 동안 0 → 150명 램핑 (총 150)
					rampUsers(1000).during(Duration.ofMinutes(1)),
					// 3분 동안 초당 10명 유지 (총 10 × 210 = 2,100)
					constantUsersPerSec(30).during(Duration.ofMinutes(4)).randomized(),
					// 1분 동안 0 → 2,750명 램핑 (총 2,750)
					rampUsers(1800).during(Duration.ofMinutes(1))
				)
			).protocols(httpProtocol);
		}



	}
}
