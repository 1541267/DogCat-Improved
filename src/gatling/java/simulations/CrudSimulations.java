package simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class CrudSimulations extends Simulation {

	// http 프로토콜 설정
	HttpProtocolBuilder httpProtocol = http
		.baseUrl("https://localhost:10000")
		.acceptHeader("application/json")
		.contentTypeHeader("application/json");

	// 시나리오 정의
	ScenarioBuilder scn = scenario("말랑발자국 CRUD 부하 테스트")
		// .exec(
		// 	http("CreatePost")
		// 		.post("/posts")
		// 		.body(StringBody(
		// 			"{\"boardCode\":\"general\",\"postTitle\":\"성능테스트\",\"postContent\":\"" +
		// 				"x".repeat(500) +
		// 				"\",\"userId\":\"test@1.com\"}"
		// 		)).asJson()
		// 		.check(status().is(201), jsonPath("$.postNo").saveAs("postNo"))
		// )
		// .pause(Duration.ofMillis(200))

		// 게시글 조회, /posts/{postNo}
		.exec(
			http("GetPost")
				.get("/posts/${postNo}")
				.check(status().is(200))
		)
		.pause(Duration.ofMillis(200))

		// Update
		.exec(
			http("UpdatePost")
				.put("/posts/${postNo}")
				.body(StringBody(
								"{\"boardCode\":\"general\",\"postTitle\":\"성능, 수정테스트\",\"postContent\":\"" +
									"x".repeat(500) +
									"\",\"userId\":\"test@1.com\"}"
							)).asJson()
				.check(status().is(200))
		)
		.pause(Duration.ofMillis(200))


		// Delete
		.exec(
			http("DeletePost")
				.delete("/posts/${postNo}")
				.check(status().is(204))
		);


	{
		setUp(
			scn.injectOpen(
				rampUsers(50).during(Duration.ofSeconds(20)),
				constantUsersPerSec(20).during(Duration.ofMinutes(1))
			)
		).protocols(httpProtocol);
	}

}
