(function () {
    'use strict';

    const API_BASE = '/api';

    const STATUS_LABELS = {
        WAITING: '대기',
        MARKDOWN_IN_PROGRESS: '마크다운-진행',
        CHUNKING_IN_PROGRESS: '청킹-진행',
        EMBEDDING_IN_PROGRESS: '임베딩-진행',
        COMPLETED: '완료'
    };

    const documentId = window.location.pathname.split('/').pop();

    const detailLoading = document.getElementById('detail-loading');
    const detailError = document.getElementById('detail-error');
    const detail = document.getElementById('detail');

    const docName = document.getElementById('doc-name');
    const docStatus = document.getElementById('doc-status');
    const docMeta = document.getElementById('doc-meta');
    const docDescription = document.getElementById('doc-description');
    const docUrl = document.getElementById('doc-url');
    const docFile = document.getElementById('doc-file');

    const analysisLoading = document.getElementById('analysis-loading');
    const analysisError = document.getElementById('analysis-error');
    const analysisResult = document.getElementById('analysis-result');

    const markdownEmpty = document.getElementById('markdown-empty');
    const markdownPanel = document.getElementById('markdown-panel');
    const markdownRendered = document.getElementById('markdown-rendered');
    const markdownRaw = document.getElementById('markdown-raw');
    const mdViewRendered = document.getElementById('md-view-rendered');
    const mdViewRaw = document.getElementById('md-view-raw');

    const chunksEmpty = document.getElementById('chunks-empty');
    const chunksTableWrap = document.getElementById('chunks-table-wrap');
    const chunkCount = document.getElementById('chunk-count');
    const chunkRows = document.getElementById('chunk-rows');

    function formatBytes(bytes) {
        if (bytes < 1024) {
            return bytes + ' B';
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + ' KB';
        }
        return (bytes / 1024 / 1024).toFixed(1) + ' MB';
    }

    function formatTime(iso) {
        if (!iso) {
            return '-';
        }
        const d = new Date(iso);
        return isNaN(d.getTime()) ? iso : d.toLocaleString('ko-KR');
    }

    function syncMarkdownView() {
        markdownRendered.classList.toggle('d-none', !mdViewRendered.checked);
        markdownRaw.classList.toggle('d-none', mdViewRendered.checked);
    }

    mdViewRendered.addEventListener('change', syncMarkdownView);
    mdViewRaw.addEventListener('change', syncMarkdownView);

    function renderDetail(item) {
        docName.textContent = item.name;
        docStatus.textContent = STATUS_LABELS[item.statusType] || item.statusType;
        docMeta.textContent = 'ID ' + item.id + ' · 등록 ' + formatTime(item.createdAt);
        docDescription.textContent = item.description || '-';

        if (item.url) {
            const a = document.createElement('a');
            a.href = item.url;
            a.target = '_blank';
            a.rel = 'noopener noreferrer';
            a.textContent = item.url;
            docUrl.replaceChildren(a);
        } else {
            docUrl.textContent = '-';
        }

        docFile.textContent = item.file
                ? item.file.originalName + ' · ' + formatBytes(item.file.size)
                : '-';

        detailLoading.classList.add('d-none');
        detail.classList.remove('d-none');
    }

    function renderAnalysis(result) {
        if (result.markdown) {
            window.renderMarkdown(markdownRendered, result.markdown);
            markdownRaw.textContent = result.markdown;
            markdownPanel.classList.remove('d-none');
            markdownEmpty.classList.add('d-none');
        } else {
            markdownPanel.classList.add('d-none');
            markdownEmpty.classList.remove('d-none');
        }

        chunkCount.textContent = result.chunkCount;
        chunkRows.replaceChildren();

        if (!result.chunks.length) {
            chunksTableWrap.classList.add('d-none');
            chunksEmpty.classList.remove('d-none');
        } else {
            chunksTableWrap.classList.remove('d-none');
            chunksEmpty.classList.add('d-none');

            result.chunks.forEach(function (chunk) {
                const tr = document.createElement('tr');

                const idx = document.createElement('th');
                idx.scope = 'row';
                idx.className = 'text-end fw-normal';
                idx.textContent = chunk.index;
                tr.appendChild(idx);

                const chars = document.createElement('td');
                chars.className = 'text-end';
                chars.textContent = chunk.chars.toLocaleString('ko-KR');
                tr.appendChild(chars);

                const tokens = document.createElement('td');
                tokens.className = 'text-end';
                tokens.textContent = chunk.tokens.toLocaleString('ko-KR');
                tr.appendChild(tokens);

                const text = document.createElement('td');
                text.className = 'small markdown-cell';
                window.renderMarkdown(text, chunk.text);
                tr.appendChild(text);

                chunkRows.appendChild(tr);
            });
        }

        analysisLoading.classList.add('d-none');
        analysisResult.classList.remove('d-none');
    }

    async function load() {
        try {
            const response = await fetch(API_BASE + '/documents/' + documentId);
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            renderDetail(await response.json());
        } catch (err) {
            detailLoading.classList.add('d-none');
            detailError.textContent = '문서를 불러오지 못했습니다 — ' + err.message;
            detailError.classList.remove('d-none');
            return;
        }

        try {
            const response = await fetch(API_BASE + '/documents/' + documentId + '/analysis');
            const text = await response.text();
            if (!response.ok) {
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }
            renderAnalysis(JSON.parse(text));
        } catch (err) {
            analysisLoading.classList.add('d-none');
            analysisError.textContent = '마크다운/청킹 변환에 실패했습니다 — ' + err.message;
            analysisError.classList.remove('d-none');
        }
    }

    load();
})();
