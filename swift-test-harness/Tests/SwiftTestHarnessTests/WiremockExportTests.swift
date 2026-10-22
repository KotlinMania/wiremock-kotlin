import Testing
import Wiremock
import KotlinStdlib
import ExportedKotlinPackages

@Suite("Wiremock Swift Export Suite")
struct WiremockExportTests {
    @Test("Swift module loads and basic response template works")
    func swiftModuleLoads() {
        let template = ResponseTemplate(statusCode: 200)
        #expect(template.statusCode == 200)
    }

    @Test("Response header maps preserve ordered Unicode values")
    func responseHeadersRoundTrip() {
        let expected = ["X-Values": ["first", "βeta", ""], "X-Other": ["雪"]]
        let template = ResponseTemplate(statusCode: 201)
        let returned = template.appendHeaders(headers: expected)
        #expect(returned === template)
        #expect(template.headers == expected)
        _ = template.insertHeader(key: "X-Values", value: "replacement")
        #expect(template.headers["X-Values"] == ["replacement"])
        #expect(template.headers["X-Other"] == ["雪"])
    }

    @Test("Request header maps cross the typed constructor boundary")
    func requestHeadersRoundTrip() {
        let expected = ["X-Values": ["one", "two"], "雪": ["β"]]
        let request = Request(
            url: "/headers",
            method: "GET",
            headers: expected,
            body: ExportedKotlinPackages.kotlin.ByteArray(size: 0)
        )
        #expect(request.headers == expected)
        #expect(request.url == "/headers")
        #expect(request.body.size == 0)
    }
}
