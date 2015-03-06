$('.disableOnSubmit').submit(function() {
  $(this).find("button[type='submit']").prop('disabled',true);
});

$("[name='isPublic']").bootstrapSwitch();