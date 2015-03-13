var updatePaste = function(state) {
    var apiEndpoint = "api/updateVisibility"
    $.ajax({
        type: "POST",
        url: "/api/updateVisibility",
        data: JSON.stringify({ pasteId: $(idNumber).val(), isPublic: state }),
        contentType: "application/json",
        success: function(data, textStatus, jqXHR) {
            //alert(data.status + data.message);
        }
    });
};

$("[name='isPublic']").bootstrapSwitch();

$("#publicPrivateSwitch").on("switchChange.bootstrapSwitch", function(event, state) {
    updatePaste(state);
});