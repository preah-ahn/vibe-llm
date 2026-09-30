(function () {
    'use strict';

    const uploadForm = document.getElementById('upload-form');
    const fileEl = document.getElementById('file');
    const analyzeBtn = document.getElementById('analyze-btn');
    const ingestBtn = document.getElementById('ingest-btn');
    const uploadResult = document.getElementById('upload-result');

    const analysisBox = document.getElementById('analysis');
    const analysisSummary = document.getElementById('analysis-summary');
    const analysisExisting = document.getElementById('analysis-existing');
    const analysisEmpty = document.getElementById('analysis-empty');
    const chunkRows = document.getElementById('chunk-rows');

    const markdownPanel = document.getElementById('markdown-panel');
    const markdownRendered = document.getElementById('markdown-rendered');
    const markdownRaw = document.getElementById('markdown-raw');
    const mdViewRendered = document.getElementById('md-view-rendered');
    const mdViewRaw = document.getElementById('md-view-raw');

    const rows = document.getElementById('doc-rows');
    const listError = document.getElementById('list-error');
    const listEmpty = document.getElementById('list-empty');
    const refreshBtn = document.getElementById('refresh-btn');

    const deleteModalEl = document.getElementById('delete-modal');
    const deleteModal = new bootstrap.Modal(deleteModalEl);
    const deleteTarget = document.getElementById('delete-target');
    const deleteConfirm = document.getElementById('delete-confirm');
    let pendingSource = null;

    // 분석한 파일과 적재 대상이 어긋나지 않도록, 분석 시점의 File 객체를 들고 있는다.
    let analyzedFile = null;

    function alertHtml(kind, text) {
        const div = document.createElement('div');
        div.className = 'alert alert-' + kind + ' mb-0';
        div.setAttribute('role', 'alert');
        div.textContent = text;
        return div;
    }

    function showUpload(kind, text) {
        uploadResult.replaceChildren(alertHtml(kind, text));
    }

    function clearUpload() {
        uploadResult.replaceChildren();
    }

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

    // 파일이 바뀌면 이전 분석 결과는 무효다. 적재 버튼을 다시 잠근다.
    function resetAnalysis() {
        analyzedFile = null;
        ingestBtn.disabled = true;
        analysisBox.classList.add('d-none');
        analysisSummary.replaceChildren();
        analysisExisting.classList.add('d-none');
        analysisEmpty.classList.add('d-none');
        markdownRendered.replaceChildren();
        markdownRaw.textContent = '';
        chunkRows.replaceChildren();
    }

    function summaryCard(label, value) {
        const col = document.createElement('div');
        col.className = 'col-6 col-lg-3';

        const box = document.createElement('div');
        box.className = 'border rounded p-2 h-100';

        const dt = document.createElement('div');
        dt.className = 'small text-body-secondary';
        dt.textContent = label;

        const dd = document.createElement('div');
        dd.className = 'fw-semibold text-break';
        dd.textContent = value;

        box.append(dt, dd);
        col.appendChild(box);
        return col;
    }

    function renderAnalysis(result) {
        analysisSummary.replaceChildren(
            summaryCard('파일', result.source + ' · ' + formatBytes(result.sizeBytes)),
            summaryCard('청크', result.chunkCount + '개'),
            summaryCard('추출 텍스트', result.textLength.toLocaleString('ko-KR') + '자 · '
                    + result.totalTokens.toLocaleString('ko-KR') + '토큰'),
            summaryCard('임베딩', (result.embeddingModel || '미설정') + ' · ' + result.dimensions + '차원')
        );

        if (result.existingChunks > 0) {
            analysisExisting.textContent = '이미 적재된 문서입니다 — 기존 '
                    + result.existingChunks + '개 청크(' + formatTime(result.existingIngestedAt)
                    + ')를 삭제하고 교체합니다.';
            analysisExisting.classList.remove('d-none');
        } else {
            analysisExisting.classList.add('d-none');
        }

        analysisEmpty.classList.toggle('d-none', result.chunkCount > 0);

        renderMarkdown(markdownRendered, result.markdown);
        markdownRaw.textContent = result.markdown || '';
        markdownPanel.classList.toggle('d-none', !result.markdown);

        chunkRows.replaceChildren();
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
            renderMarkdown(text, chunk.text);
            tr.appendChild(text);

            chunkRows.appendChild(tr);
        });

        analysisBox.classList.remove('d-none');
    }

    async function loadList() {
        listError.classList.add('d-none');
        listEmpty.classList.add('d-none');
        rows.replaceChildren(loadingRow());

        try {
            const response = await fetch('/documents');
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            render(await response.json());
        } catch (err) {
            rows.replaceChildren();
            listError.textContent = '목록을 불러오지 못했습니다 — ' + err.message;
            listError.classList.remove('d-none');
        }
    }

    function loadingRow() {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 4;
        td.className = 'text-body-secondary';
        td.textContent = '불러오는 중…';
        tr.appendChild(td);
        return tr;
    }

    function render(items) {
        rows.replaceChildren();

        if (!items.length) {
            listEmpty.classList.remove('d-none');
            return;
        }

        items.forEach(function (item) {
            const tr = document.createElement('tr');

            const name = document.createElement('td');
            name.className = 'text-break';
            name.textContent = item.source === null ? '(source 없음)' : item.source;
            tr.appendChild(name);

            const chunks = document.createElement('td');
            chunks.className = 'text-end';
            chunks.textContent = item.chunks;
            tr.appendChild(chunks);

            const at = document.createElement('td');
            at.className = 'text-body-secondary small';
            at.textContent = formatTime(item.ingestedAt);
            tr.appendChild(at);

            const actions = document.createElement('td');
            actions.className = 'text-end';
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn btn-sm btn-outline-danger';
            btn.textContent = '삭제';
            btn.addEventListener('click', function () {
                pendingSource = item.source;
                deleteTarget.textContent = item.source;
                deleteModal.show();
            });
            actions.appendChild(btn);
            tr.appendChild(actions);

            rows.appendChild(tr);
        });
    }

    function syncMarkdownView() {
        markdownRendered.classList.toggle('d-none', !mdViewRendered.checked);
        markdownRaw.classList.toggle('d-none', mdViewRendered.checked);
    }

    mdViewRendered.addEventListener('change', syncMarkdownView);
    mdViewRaw.addEventListener('change', syncMarkdownView);

    fileEl.addEventListener('change', function () {
        fileEl.classList.remove('is-invalid');
        clearUpload();
        resetAnalysis();
    });

    // 1단계 — 분석하기
    uploadForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        if (!fileEl.files.length) {
            fileEl.classList.add('is-invalid');
            return;
        }
        fileEl.classList.remove('is-invalid');

        const file = fileEl.files[0];
        const body = new FormData();
        body.append('file', file);

        analyzeBtn.disabled = true;
        resetAnalysis();
        showUpload('secondary', '분석 중… docling 파싱과 청킹만 수행하며 저장하지 않습니다. CPU 변환은 수십 초가 걸릴 수 있습니다.');

        try {
            const response = await fetch('/documents/analysis', { method: 'POST', body: body });
            const text = await response.text();
            if (!response.ok) {
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }

            const result = JSON.parse(text);
            renderAnalysis(result);
            clearUpload();

            analyzedFile = file;
            ingestBtn.disabled = false;
        } catch (err) {
            showUpload('danger', '분석 실패 — ' + err.message);
        } finally {
            analyzeBtn.disabled = false;
        }
    });

    // 2단계 — 적재하기
    ingestBtn.addEventListener('click', async function () {
        if (analyzedFile === null) {
            return;
        }

        const body = new FormData();
        body.append('file', analyzedFile);

        analyzeBtn.disabled = true;
        ingestBtn.disabled = true;
        showUpload('secondary', '적재 중… 임베딩 호출이 포함되어 시간이 걸릴 수 있습니다.');

        try {
            const response = await fetch('/documents', { method: 'POST', body: body });
            const text = await response.text();
            if (!response.ok) {
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }

            const result = JSON.parse(text);
            if (result.chunks === 0) {
                // 스캔 PDF 처럼 텍스트 레이어가 없으면 저장할 청크가 없다.
                showUpload('warning', result.source + ' — 추출된 텍스트가 없어 저장된 청크가 0개입니다.');
            } else {
                showUpload('success', result.source + ' — ' + result.chunks + '개 청크를 저장했습니다.');
            }

            uploadForm.reset();
            resetAnalysis();
            await loadList();
        } catch (err) {
            showUpload('danger', '적재 실패 — ' + err.message);
            ingestBtn.disabled = false;
        } finally {
            analyzeBtn.disabled = false;
        }
    });

    deleteConfirm.addEventListener('click', async function () {
        if (pendingSource === null) {
            return;
        }

        deleteConfirm.disabled = true;
        try {
            const response = await fetch('/documents?source=' + encodeURIComponent(pendingSource), {
                method: 'DELETE'
            });
            const text = await response.text();
            if (!response.ok) {
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }

            const result = JSON.parse(text);
            showUpload('success', result.source + ' — ' + result.deleted + '개 청크를 삭제했습니다.');
            await loadList();
        } catch (err) {
            showUpload('danger', '삭제 실패 — ' + err.message);
        } finally {
            deleteConfirm.disabled = false;
            pendingSource = null;
            deleteModal.hide();
        }
    });

    refreshBtn.addEventListener('click', loadList);
    loadList();
})();
