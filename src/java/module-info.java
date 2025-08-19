module com.BluePrintHell {
    // نیازمندی‌های اصلی
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    // اجازه می‌دهد FXML به کنترلرها دسترسی داشته باشد
    opens com.BluePrintHell.controller to javafx.fxml;

    // اجازه می‌دهد کتابخانه Jackson به کلاس‌های دیتا دسترسی داشته باشد
    opens com.BluePrintHell.model.leveldata to com.fasterxml.jackson.databind;
    opens com.BluePrintHell.model to com.fasterxml.jackson.databind;

    // پکیج اصلی را برای اجرا شدن باز می‌کند
    exports com.BluePrintHell;
}