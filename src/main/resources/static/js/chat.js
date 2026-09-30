(function () {
    'use strict';

    const form = document.getElementById('chat-form');
    const queryEl = document.getElementById('query');
    const conversationEl = document.getElementById('conversationId');
    const sendBtn = document.getElementById('send-btn');
    const errorBox = document.getElementById('error');

    const panes = {
        rag: {
            pending: document.getElementById('rag-pending'),
            empty: document.getElementById('rag-empty'),
            answer: document.getElementById('rag-answer')
        },
        plain: {
            pending: document.getElementById('plain-pending'),
            empty: document.getElementById('plain-empty'),
            answer: document.getElementById('plain-answer')
        }
    };

    function showError(message) {
        errorBox.textContent = message;
        errorBox.classList.remove('d-none');
    }

    function clearError() {
        errorBox.textContent = '';
        errorBox.classList.add('d-none');
    }

    function setPending(pane) {
        pane.empty.classList.add('d-none');
        pane.answer.classList.add('d-none');
        pane.answer.textContent = '';
        pane.pending.classList.remove('d-none');
    }

    // 첫 토큰이 도착하면 스피너를 내리고 답변 영역을 연다. 이후 렌더는 streamMarkdown 이 한다.
    function openAnswer(pane) {
        pane.pending.classList.add('d-none');
        pane.empty.classList.add('d-none');
        pane.answer.classList.remove('d-none');
    }

    // 실패 메시지는 우리가 만든 문자열이므로 렌더하지 않는다.
    function setFailed(pane, message) {
        pane.pending.classList.add('d-none');
        pane.empty.classList.add('d-none');
        pane.answer.textContent = message;
        pane.answer.classList.remove('d-none');
        pane.answer.classList.add('text-danger');
    }

    function resetPane(pane) {
        pane.pending.classList.add('d-none');
        pane.answer.classList.add('d-none');
        pane.answer.classList.remove('text-danger');
        pane.answer.textContent = '';
        pane.empty.classList.remove('d-none');
    }

    // 한글 쿼리는 반드시 인코딩해서 보낸다. 인코딩이 깨지면 Tomcat 이 400 을 반환한다.
    //
    // 실패는 본문을 스트림으로 읽기 전에 상태 코드로 가른다. 스트리밍이 시작된 뒤
    // 끊기면 그 시점까지 렌더된 내용은 남고 예외만 호출자에게 전달된다.
    async function ask(pane, url, options) {
        const response = await fetch(url, options);
        if (!response.ok) {
            const body = await response.text();
            throw new Error('HTTP ' + response.status + ' — ' + body.slice(0, 200));
        }
        await streamMarkdown(pane.answer, response, function () { openAnswer(pane); });
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        clearError();

        const query = queryEl.value.trim();
        if (!query) {
            queryEl.classList.add('is-invalid');
            return;
        }
        queryEl.classList.remove('is-invalid');

        const conversationId = conversationEl.value.trim() || 'ui-default';
        const q = encodeURIComponent(query);
        const cid = encodeURIComponent(conversationId);

        sendBtn.disabled = true;
        panes.rag.answer.classList.remove('text-danger');
        panes.plain.answer.classList.remove('text-danger');
        setPending(panes.rag);
        setPending(panes.plain);

        // 두 엔드포인트를 동시에 호출하고 각각 도착하는 대로 흘려보낸다.
        // 스트리밍 변형을 쓰는 이유는 [design.md] 참고 — /chat-streams 는 프롬프트가 달라
        // 비교에 쓸 수 없다.
        const ragCall = ask(panes.rag, '/rag-chat-streams?query=' + q + '&conversationId=' + cid)
            .catch(function (err) { setFailed(panes.rag, err.message); });

        // /plain-chat-streams 는 /chats 와 마찬가지로 userId 헤더가 필수다.
        const plainCall = ask(panes.plain, '/plain-chat-streams?query=' + q,
                { headers: { userId: conversationId } })
            .catch(function (err) { setFailed(panes.plain, err.message); });

        await Promise.allSettled([ragCall, plainCall]);
        sendBtn.disabled = false;
    });

    form.addEventListener('reset', function () {
        clearError();
        queryEl.classList.remove('is-invalid');
        resetPane(panes.rag);
        resetPane(panes.plain);
    });
})();
