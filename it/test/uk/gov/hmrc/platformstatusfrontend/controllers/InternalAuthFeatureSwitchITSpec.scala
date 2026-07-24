/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.platformstatusfrontend.controllers

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.http.test.WireMockSupport

class InternalAuthFeatureSwitchITSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with GuiceOneServerPerTest
     with WireMockSupport:

  "Internal auth feature switch" when:

    "enabled (default)" should:
      "require authentication for protected endpoints" in:
        val app = GuiceApplicationBuilder()
          .configure(
            "microservice.services.internal-auth.host" -> wireMockHost,
            "microservice.services.internal-auth.port" -> wireMockPort,
            "internalAuth.enabled" -> true
          )
          .build()

        stubFor(
          post(urlEqualTo("/internal-auth/auth"))
            .willReturn(okJson("""{"retrievals": []}"""))
        )

        val result = route(
          app,
          FakeRequest(GET, "/platform-status/noise")
            .withSession(SessionKeys.authToken -> "Token token")
        ).value

        status(result) shouldBe OK
        verify(
          postRequestedFor(urlEqualTo("/internal-auth/auth"))
            .withRequestBody(containing("platform-status-frontend"))
        )

        app.stop()
      end "require authentication for protected endpoints"

      "redirect to login when not authenticated" in:
        val app = GuiceApplicationBuilder()
          .configure(
            "microservice.services.internal-auth.host" -> wireMockHost,
            "microservice.services.internal-auth.port" -> wireMockPort,
            "internalAuth.enabled" -> true
          )
          .build()

        stubFor(
          post(urlEqualTo("/internal-auth/auth"))
            .willReturn(unauthorized())
        )

        val result = route(
          app,
          FakeRequest(GET, "/platform-status/noise")
        ).value

        status(result) shouldBe SEE_OTHER

        app.stop()
      end "redirect to login when not authenticated"
    end "enabled (default)"

    "disabled" should:
      "allow access without authentication" in:
        val app = GuiceApplicationBuilder()
          .configure(
            "microservice.services.internal-auth.host" -> wireMockHost,
            "microservice.services.internal-auth.port" -> wireMockPort,
            "internalAuth.enabled" -> false
          )
          .build()

        val result = route(
          app,
          FakeRequest(GET, "/platform-status/noise")
        ).value

        status(result) shouldBe OK
        // Verify that internal-auth was NOT called
        verify(0, postRequestedFor(urlEqualTo("/internal-auth/auth")))

        app.stop()
      end "allow access without authentication"

      "not require session token" in:
        val app = GuiceApplicationBuilder()
          .configure(
            "microservice.services.internal-auth.host" -> wireMockHost,
            "microservice.services.internal-auth.port" -> wireMockPort,
            "internalAuth.enabled" -> false
          )
          .build()

        // Request without session token
        val result = route(
          app,
          FakeRequest(GET, "/platform-status/platformstatus")
        ).value

        status(result) shouldBe OK
        // Verify that internal-auth was NOT called
        verify(0, postRequestedFor(urlEqualTo("/internal-auth/auth")))

        app.stop()
      end "not require session token"
    end "disabled"
  end "Internal auth feature switch"


