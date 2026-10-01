package io.vibe.llm.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @since       2026.09.30
 * @author      preah
 * @description ui controller
 **********************************************************************************************************************/
/**
 * 화면 라우팅만 담당한다. 실제 동작은 브라우저에서 기존 REST 엔드포인트를 호출한다.
 * 서비스 호출 경로를 REST 와 이원화하지 않기 위한 의도적인 구조다.
 */
@Controller
public class UiController {

    @GetMapping("/")
    public String root() {
        return "redirect:/ui";
    }

    @GetMapping("/ui")
    public String chat() {
        return "chat";
    }

    @GetMapping("/ui/documents")
    public String documents() {
        return "documents";
    }

    @GetMapping("/ui/stream")
    public String stream() {
        return "stream";
    }
}
