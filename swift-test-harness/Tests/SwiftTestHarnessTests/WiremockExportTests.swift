import Testing
import Wiremock

@Suite("Wiremock Swift Export Suite")
struct WiremockExportTests {
    @Test("Swift module loads and basic response template works")
    func swiftModuleLoads() {
        let template = ResponseTemplate(statusCode: 200)
        #expect(template.statusCode == 200)
    }
}
