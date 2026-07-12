package common;

public class Result<T> {

    private final boolean success;
    private final String message;
    //بدون Generic مجبور بودیم برای هر خروجی یک کلاس جدا بنویسیم.
    private final T data;

    private Result(boolean success, String message, T data) {

        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, message, data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(true, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

}