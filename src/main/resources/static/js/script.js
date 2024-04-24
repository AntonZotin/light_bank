function onChangeAccount(account) {
    changeUrl('account', account, 'Select account');
}

function onChangePurpose(prp) {
    changeUrl('purpose', prp, 'Select purpose');
}

function onChangePage(pg) {
    changeUrl('page', pg, null);
}

function onChangeRole(rl) {
    changeUrl('role', rl, 'Select role');
}

function changeUrl(key, value, defaultValue) {
    const url = new URL(window.location);
    if (value === defaultValue) {
        url.searchParams.delete(key);
    } else {
        url.searchParams.set(key, value);
    }
    window.location.href = url.toString();
}
