/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.jwt;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * @author Rob Winch
 * @since 5.1
 */
public class NimbusJwkReactiveJwtDecoderTests {

	private String expired = "eyJraWQiOiJrZXktaWQtMSIsImFsZyI6IlJTMjU2In0.eyJzY29wZSI6Im1lc3NhZ2U6cmVhZCIsImV4cCI6MTUyOTkzNzYzMX0.Dt5jFOKkB8zAmjciwvlGkj4LNStXWH0HNIfr8YYajIthBIpVgY5Hg_JL8GBmUFzKDgyusT0q60OOg8_Pdi4Lu-VTWyYutLSlNUNayMlyBaVEWfyZJnh2_OwMZr1vRys6HF-o1qZldhwcfvczHg61LwPa1ISoqaAltDTzBu9cGISz2iBUCuR0x71QhbuRNyJdjsyS96NqiM_TspyiOSxmlNch2oAef1MssOQ23CrKilIvEDsz_zk5H94q7rH0giWGdEHCENESsTJS0zvzH6r2xIWjd5WnihFpCPkwznEayxaEhrdvJqT_ceyXCIfY4m3vujPQHNDG0UshpwvDuEbPUg";

	private String messageReadToken = "eyJraWQiOiJrZXktaWQtMSIsImFsZyI6IlJTMjU2In0.eyJzY29wZSI6Im1lc3NhZ2U6cmVhZCIsImV4cCI6OTIyMzM3MjAwNjA5NjM3NX0.bnQ8IJDXmQbmIXWku0YT1HOyV_3d0iQSA_0W2CmPyELhsxFETzBEEcZ0v0xCBiswDT51rwD83wbX3YXxb84fM64AhpU8wWOxLjha4J6HJX2JnlG47ydaAVD7eWGSYTavyyQ-CwUjQWrfMVcObFZLYG11ydzRYOR9-aiHcK3AobcTcS8jZFeI8EGQV_Cd3IJ018uFCf6VnXLv7eV2kRt08Go2RiPLW47ExvD7Dzzz_wDBKfb4pNem7fDvuzB3UPcp5m9QvLZicnbS_6AvDi6P1y_DFJf-1T5gkGmX5piDH1L1jg2Yl6tjmXbk5B3VhsyjJuXE6gzq1d-xie0Z1NVOxw";

	private String jwkSet =
		"{\n"
		+ "   \"keys\":[\n"
		+ "      {\n"
		+ "         \"kty\":\"RSA\",\n"
		+ "         \"e\":\"AQAB\",\n"
		+ "         \"use\":\"sig\",\n"
		+ "         \"kid\":\"key-id-1\",\n"
		+ "         \"n\":\"qL48v1clgFw-Evm145pmh8nRYiNt72Gupsshn7Qs8dxEydCRp1DPOV_PahPk1y2nvldBNIhfNL13JOAiJ6BTiF-2ICuICAhDArLMnTH61oL1Hepq8W1xpa9gxsnL1P51thvfmiiT4RTW57koy4xIWmIp8ZXXfYgdH2uHJ9R0CQBuYKe7nEOObjxCFWC8S30huOfW2cYtv0iB23h6w5z2fDLjddX6v_FXM7ktcokgpm3_XmvT_-bL6_GGwz9k6kJOyMTubecr-WT__le8ikY66zlplYXRQh6roFfFCL21Pt8xN5zrk-0AMZUnmi8F2S2ztSBmAVJ7H71ELXsURBVZpw\"\n"
		+ "      }\n"
		+ "   ]\n"
		+ "}";

	private MockWebServer server;
	private NimbusJwkReactiveJwtDecoder decoder;

	@Before
	public void setup() throws Exception {
		this.server = new MockWebServer();
		this.server.start();
		this.server.enqueue(new MockResponse().setBody(jwkSet));
		this.decoder = new NimbusJwkReactiveJwtDecoder(this.server.url("/certs").toString());
	}

	@After
	public void cleanup() throws Exception {
		this.server.shutdown();
	}

	@Test
	public void decodeWhenMessageReadScopeThenSuccess() {
		Jwt jwt = this.decoder.decode(this.messageReadToken).block();

		assertThat(jwt.getClaims().get("scope")).isEqualTo("message:read");
	}

	@Test
	public void decodeWhenIssuedAtThenSuccess() {
		String withIssuedAt = "eyJraWQiOiJrZXktaWQtMSIsImFsZyI6IlJTMjU2In0.eyJzY29wZSI6IiIsImV4cCI6OTIyMzM3MjAwNjA5NjM3NSwiaWF0IjoxNTI5OTQyNDQ4fQ.LBzAJO-FR-uJDHST61oX4kimuQjz6QMJPW_mvEXRB6A-fMQWpfTQ089eboipAqsb33XnwWth9ELju9HMWLk0FjlWVVzwObh9FcoKelmPNR8mZIlFG-pAYGgSwi8HufyLabXHntFavBiFtqwp_z9clSOFK1RxWvt3lywEbGgtCKve0BXOjfKWiH1qe4QKGixH-NFxidvz8Qd5WbJwyb9tChC6ZKoKPv7Jp-N5KpxkY-O2iUtINvn4xOSactUsvKHgF8ZzZjvJGzG57r606OZXaNtoElQzjAPU5xDGg5liuEJzfBhvqiWCLRmSuZ33qwp3aoBnFgEw0B85gsNe3ggABg";

		Jwt jwt = this.decoder.decode(withIssuedAt).block();

		assertThat(jwt.getClaims().get(JwtClaimNames.IAT)).isEqualTo(new Date(1529942448000L));
	}

	@Test
	public void decodeWhenExpiredThenFail() {
		assertThatCode(() -> this.decoder.decode(this.expired).block())
				.isInstanceOf(JwtException.class);
	}

	@Test
	public void decodeWhenNoPeriodThenFail() {
		assertThatCode(() -> this.decoder.decode("").block())
				.isInstanceOf(JwtException.class);
	}

	@Test
	public void decodeWhenInvalidJwkSetUrlThenFail() {
		this.decoder = new NimbusJwkReactiveJwtDecoder("http://localhost:1280/certs");
		assertThatCode(() -> this.decoder.decode(this.messageReadToken).block())
				.isInstanceOf(JwtException.class);
	}

	@Test
	public void decodeWhenInvalidSignatureThenFail() {
		assertThatCode(() -> this.decoder.decode(this.messageReadToken.substring(0, this.messageReadToken.length() - 2)).block())
				.isInstanceOf(JwtException.class);
	}

	@Test
	public void decodeWhenAlgNoneThenFail() {
		assertThatCode(() -> this.decoder.decode("ew0KICAiYWxnIjogIm5vbmUiLA0KICAidHlwIjogIkpXVCINCn0.ew0KICAic3ViIjogIjEyMzQ1Njc4OTAiLA0KICAibmFtZSI6ICJKb2huIERvZSIsDQogICJpYXQiOiAxNTE2MjM5MDIyDQp9.").block())
			.isInstanceOf(JwtException.class)
			.hasMessage("Unsupported algorithm of none");
	}

	@Test
	public void decodeWhenInvalidAlgMismatchThenFail() {
		assertThatCode(() -> this.decoder.decode("ew0KICAiYWxnIjogIkVTMjU2IiwNCiAgInR5cCI6ICJKV1QiDQp9.ew0KICAic3ViIjogIjEyMzQ1Njc4OTAiLA0KICAibmFtZSI6ICJKb2huIERvZSIsDQogICJpYXQiOiAxNTE2MjM5MDIyDQp9.").block())
				.isInstanceOf(JwtException.class);
	}
}
