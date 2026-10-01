(function () {
    'use strict';

    const API_BASE = '/api';
    const PAGE_SIZE = 20;

    const STATUS_LABELS = {
        WAITING: '대기',
        MARKDOWN_IN_PROGRESS: '마크다운-진행',
        CHUNKING_IN_PROGRESS: '청킹-진행',
        EMBEDDING_IN_PROGRESS: '임베딩-진행',
        COMPLETED: '완료'
    };

    const searchForm = document.getElementById('search-form');
    const searchName = document.getElementById('search-name');
    const addBtn = document.getElementById('add-btn');

    const rows = document.getElementById('doc-rows');
    const listError = document.getElementById('list-error');
    const listEmpty = document.getElementById('list-empty');

    const paginationBar = document.getElementById('pagination-bar');
    const paginationInfo = document.getElementById('pagination-info');
    const pagePrev = document.getElementById('page-prev');
    const pageNext = document.getElementById('page-next');

    const documentModalEl = document.getElementById('document-modal');
    const documentModal = new bootstrap.Modal(documentModalEl);
    const documentModalTitle = document.getElementById('document-modal-title');
    const documentForm = document.getElementById('document-form');
    const documentFormError = document.getElementById('document-form-error');
    const docNameEl = document.getElementById('doc-name');
    const docDescriptionEl = document.getElementById('doc-description');
    const docUrlEl = document.getElementById('doc-url');
    const docFileEl = document.getElementById('doc-file');
    const docFileCurrent = document.getElementById('doc-file-current');
    const documentSaveBtn = document.getElementById('document-save-btn');

    const deleteModalEl = document.getElementById('delete-modal');
    const deleteModal = new bootstrap.Modal(deleteModalEl);
    const deleteTarget = document.getElementById('delete-target');
    const deleteConfirm = document.getElementById('delete-confirm');

    let currentPage = 0;
    let currentName = '';
    let lastPageData = null;
    let editingId = null;
    let existingFile = null;
    let pendingDeleteId = null;

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

    function loadingRow() {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = 6;
        td.className = 'text-body-secondary';
        td.textContent = '불러오는 중…';
        tr.appendChild(td);
        return tr;
    }

    async function loadList() {
        listError.classList.add('d-none');
        listEmpty.classList.add('d-none');
        rows.replaceChildren(loadingRow());

        const params = new URLSearchParams();
        if (currentName) {
            params.set('name', currentName);
        }
        params.set('page', currentPage);
        params.set('size', PAGE_SIZE);
        params.set('sort', 'createdAt,desc');

        try {
            const response = await fetch(API_BASE + '/documents/pages?' + params.toString());
            const text = await response.text();
            if (!response.ok) {
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }
            lastPageData = JSON.parse(text);
            render(lastPageData);
        } catch (err) {
            rows.replaceChildren();
            paginationBar.classList.add('d-none');
            listError.textContent = '목록을 불러오지 못했습니다 — ' + err.message;
            listError.classList.remove('d-none');
        }
    }

    function statusSelect(item) {
        const select = document.createElement('select');
        select.className = 'form-select form-select-sm';
        Object.keys(STATUS_LABELS).forEach(function (key) {
            const option = document.createElement('option');
            option.value = key;
            option.textContent = STATUS_LABELS[key];
            if (key === item.statusType) {
                option.selected = true;
            }
            select.appendChild(option);
        });

        select.addEventListener('change', async function () {
            const previous = item.statusType;
            select.disabled = true;
            try {
                const response = await fetch(API_BASE + '/documents/' + item.id + '/status', {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ statusType: select.value })
                });
                if (!response.ok) {
                    const text = await response.text();
                    throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
                }
                item.statusType = select.value;
            } catch (err) {
                select.value = previous;
                listError.textContent = '상태 변경 실패 — ' + err.message;
                listError.classList.remove('d-none');
            } finally {
                select.disabled = false;
            }
        });

        return select;
    }

    function render(pageData) {
        rows.replaceChildren();

        const items = pageData.content || [];
        if (!items.length) {
            listEmpty.classList.remove('d-none');
            paginationBar.classList.add('d-none');
            return;
        }

        items.forEach(function (item) {
            const tr = document.createElement('tr');
            tr.style.cursor = 'pointer';
            tr.addEventListener('click', function (event) {
                if (event.target.closest('button') || event.target.closest('select')) {
                    return;
                }
                window.location.href = '/ui/documents/' + item.id;
            });

            const id = document.createElement('td');
            id.textContent = item.id;
            tr.appendChild(id);

            const name = document.createElement('td');
            name.className = 'text-break';
            name.textContent = item.name;
            tr.appendChild(name);

            const status = document.createElement('td');
            status.appendChild(statusSelect(item));
            tr.appendChild(status);

            const file = document.createElement('td');
            file.className = 'small text-break';
            file.textContent = item.file ? item.file.originalName + ' · ' + formatBytes(item.file.size) : '-';
            tr.appendChild(file);

            const at = document.createElement('td');
            at.className = 'text-body-secondary small';
            at.textContent = formatTime(item.createdAt);
            tr.appendChild(at);

            const actions = document.createElement('td');
            actions.className = 'text-end text-nowrap';

            const editBtn = document.createElement('button');
            editBtn.type = 'button';
            editBtn.className = 'btn btn-sm btn-outline-secondary me-1';
            editBtn.textContent = '수정';
            editBtn.addEventListener('click', function () {
                openEditModal(item.id);
            });
            actions.appendChild(editBtn);

            const deleteBtn = document.createElement('button');
            deleteBtn.type = 'button';
            deleteBtn.className = 'btn btn-sm btn-outline-danger';
            deleteBtn.textContent = '삭제';
            deleteBtn.addEventListener('click', function () {
                pendingDeleteId = item.id;
                deleteTarget.textContent = item.name;
                deleteModal.show();
            });
            actions.appendChild(deleteBtn);

            tr.appendChild(actions);
            rows.appendChild(tr);
        });

        renderPagination(pageData);
    }

    function renderPagination(pageData) {
        paginationBar.classList.remove('d-none');
        paginationInfo.textContent = '전체 ' + pageData.totalElements + '건 · '
                + (pageData.number + 1) + ' / ' + Math.max(pageData.totalPages, 1) + ' 페이지';
        pagePrev.disabled = pageData.first;
        pageNext.disabled = pageData.last;
    }

    pagePrev.addEventListener('click', function () {
        if (currentPage > 0) {
            currentPage -= 1;
            loadList();
        }
    });

    pageNext.addEventListener('click', function () {
        if (lastPageData && !lastPageData.last) {
            currentPage += 1;
            loadList();
        }
    });

    searchForm.addEventListener('submit', function (event) {
        event.preventDefault();
        currentName = searchName.value.trim();
        currentPage = 0;
        loadList();
    });

    function resetDocumentForm() {
        documentForm.reset();
        documentFormError.classList.add('d-none');
        docNameEl.classList.remove('is-invalid');
        docFileEl.classList.remove('is-invalid');
        docFileCurrent.textContent = '';
        existingFile = null;
    }

    function openAddModal() {
        editingId = null;
        resetDocumentForm();
        documentModalTitle.textContent = '새 문서';
        documentModal.show();
    }

    async function openEditModal(id) {
        resetDocumentForm();
        documentModalTitle.textContent = '문서 수정';

        try {
            const response = await fetch(API_BASE + '/documents/' + id);
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            const item = await response.json();

            editingId = id;
            docNameEl.value = item.name || '';
            docDescriptionEl.value = item.description || '';
            docUrlEl.value = item.url || '';
            existingFile = item.file || null;
            docFileCurrent.textContent = existingFile
                    ? '현재 파일: ' + existingFile.originalName + ' · ' + formatBytes(existingFile.size) + ' (선택 시 교체)'
                    : '';

            documentModal.show();
        } catch (err) {
            listError.textContent = '문서를 불러오지 못했습니다 — ' + err.message;
            listError.classList.remove('d-none');
        }
    }

    async function uploadAttach(file) {
        const body = new FormData();
        body.append('file', file);

        const response = await fetch('/attaches', { method: 'POST', body: body });
        const text = await response.text();
        if (!response.ok) {
            throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
        }
        return JSON.parse(text);
    }

    addBtn.addEventListener('click', openAddModal);

    documentForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        const name = docNameEl.value.trim();
        const selectedFile = docFileEl.files[0] || null;

        let valid = true;
        docNameEl.classList.toggle('is-invalid', !name);
        valid = valid && !!name;
        docFileEl.classList.toggle('is-invalid', !selectedFile && !existingFile);
        valid = valid && (!!selectedFile || !!existingFile);
        if (!valid) {
            return;
        }

        documentFormError.classList.add('d-none');
        documentSaveBtn.disabled = true;

        try {
            const fileMeta = selectedFile ? await uploadAttach(selectedFile) : existingFile;
            const payload = {
                name: name,
                description: docDescriptionEl.value.trim() || null,
                url: docUrlEl.value.trim() || null,
                file: {
                    path: fileMeta.path,
                    name: fileMeta.name,
                    originalName: fileMeta.originalName,
                    size: fileMeta.size
                }
            };

            const response = editingId === null
                    ? await fetch(API_BASE + '/documents', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    })
                    : await fetch(API_BASE + '/documents/' + editingId, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });

            const text = await response.text();
            if (!response.ok) {
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }

            documentModal.hide();
            await loadList();
        } catch (err) {
            documentFormError.textContent = '저장 실패 — ' + err.message;
            documentFormError.classList.remove('d-none');
        } finally {
            documentSaveBtn.disabled = false;
        }
    });

    deleteConfirm.addEventListener('click', async function () {
        if (pendingDeleteId === null) {
            return;
        }

        deleteConfirm.disabled = true;
        try {
            const response = await fetch(API_BASE + '/documents/' + pendingDeleteId, { method: 'DELETE' });
            if (!response.ok) {
                const text = await response.text();
                throw new Error('HTTP ' + response.status + ' — ' + text.slice(0, 200));
            }
            await loadList();
        } catch (err) {
            listError.textContent = '삭제 실패 — ' + err.message;
            listError.classList.remove('d-none');
        } finally {
            deleteConfirm.disabled = false;
            pendingDeleteId = null;
            deleteModal.hide();
        }
    });

    loadList();
})();
