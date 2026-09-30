(function () {
    'use strict';

    const form = document.getElementById('stream-form');
    const conceptEl = document.getElementById('concept');
    const conversationEl = document.getElementById('stream-conversation');
    const btn = document.getElementById('stream-btn');
    const output = document.getElementById('stream-output');
    const status = document.getElementById('stream-status');
    const errorBox = document.getElementById('stream-error');

    function setStatus(text, kind) {
        status.textContent = text;
        status.className = 'badge text-bg-' + kind;
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        errorBox.classList.add('d-none');

        const concept = conceptEl.value.trim();
        if (!concept) {
            conceptEl.classList.add('is-invalid');
            return;
        }
        conceptEl.classList.remove('is-invalid');

        const conversationId = conversationEl.value.trim() || 'ui-stream';
        const url = '/chat-streams?query=' + encodeURIComponent(concept)
                + '&conversationId=' + encodeURIComponent(conversationId);

        btn.disabled = true;
        output.textContent = '';
        setStatus('연결 중', 'secondary');

        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }

            setStatus('수신 중', 'primary');
            await streamMarkdown(output, response);
            setStatus('완료', 'success');
        } catch (err) {
            errorBox.textContent = '스트리밍 실패 — ' + err.message;
            errorBox.classList.remove('d-none');
            setStatus('실패', 'danger');
        } finally {
            btn.disabled = false;
        }
    });
})();
