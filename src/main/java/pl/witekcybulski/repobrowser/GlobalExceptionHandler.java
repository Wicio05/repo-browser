package pl.witekcybulski.repobrowser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
class GlobalExceptionHandler
{
	@ExceptionHandler(HttpClientErrorException.NotFound.class)
	ResponseEntity<ErrorResponse> handleUserNotFound(HttpClientErrorException.NotFound ex)
	{
		ErrorResponse errorResponse = new ErrorResponse(
			HttpStatus.NOT_FOUND.value(),
			"Github user does not exist with given login"
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(HttpClientErrorException.class)
	ResponseEntity<ErrorResponse> handleOtherGithubErrors(HttpClientErrorException ex)
	{
		ErrorResponse errorResponse = new ErrorResponse(
			ex.getStatusCode().value(),
			"Unexpected error while communicating with GitHuba API: " + ex.getStatusText()
		);

		return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
	}
}