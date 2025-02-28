document.addEventListener("DOMContentLoaded", () => {
    const shouldAutoRefresh = document.querySelector(".matrix-auto-refresh-data").dataset.shouldAutoRefresh === "true";
    if (shouldAutoRefresh) {
        refreshPart('matrix',"./ajaxMatrix");
    }
});
