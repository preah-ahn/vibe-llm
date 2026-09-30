// 마크다운 렌더 공용 함수. chat, stream, documents 세 화면이 함께 쓴다.
//
// 렌더 대상은 LLM 응답과 docling 파싱 결과다. 둘 다 우리가 만들지 않은 문자열을
// innerHTML 에 넣는 것이므로 DOMPurify 정화는 선택이 아니다.
(function (window) {
    'use strict';

    // 토큰마다 전체를 다시 파싱하면 응답이 길어질수록 느려진다.
    const RENDER_INTERVAL_MS = 100;

    window.renderMarkdown = function (target, source) {
        target.innerHTML = DOMPurify.sanitize(marked.parse(source || ''));
    };

    /**
     * 응답 본문 스트림을 읽어 도착한 조각을 누적하고 마크다운으로 렌더한다.
     *
     * 마크다운은 부분 문자열만으로 파싱할 수 없다 — 열린 코드 펜스나 표 헤더가 청크
     * 경계에 걸리면 조각만 봐서는 의미가 정해지지 않는다. 그래서 매번 누적 전체를
     * 다시 렌더하되 간격을 제한한다.
     *
     * @param onFirstChunk 첫 조각이 도착한 시점에 한 번 호출된다. 스피너를 내릴 때 쓴다.
     */
    window.streamMarkdown = async function (target, response, onFirstChunk) {
        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');

        let raw = '';
        let lastRender = 0;
        let pendingFirst = true;

        for (;;) {
            const chunk = await reader.read();
            if (chunk.done) {
                break;
            }

            // stream: true 를 주어 멀티바이트 한글이 청크 경계에서 깨지지 않게 한다.
            raw += decoder.decode(chunk.value, { stream: true });

            if (pendingFirst) {
                pendingFirst = false;
                if (onFirstChunk) {
                    onFirstChunk();
                }
            }

            const now = Date.now();
            if (now - lastRender >= RENDER_INTERVAL_MS) {
                lastRender = now;
                window.renderMarkdown(target, raw);
                target.scrollTop = target.scrollHeight;
            }
        }

        // 남은 바이트를 비우고, 제한 없이 한 번 더 렌더해 마지막 조각 누락을 막는다.
        raw += decoder.decode();
        window.renderMarkdown(target, raw);
        target.scrollTop = target.scrollHeight;

        return raw;
    };
})(window);
