package jp.andpad.api.graphql;

import java.util.List;

import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jp.andpad.imart.workflow.WorkflowException;
import jp.andpad.api.security.UnauthorizedException;
import org.springframework.dao.DataAccessException;
import reactor.core.publisher.Mono;

/** GraphQL データフェッチャー例外をクライアント向けエラー種別へ変換する。 */
@Component
public class GraphQlExceptionResolver implements DataFetcherExceptionResolver {

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof UnauthorizedException unauthorized) {
            return Mono.just(List.of(
                    GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.UNAUTHORIZED)
                            .message(unauthorized.getMessage())
                            .build()));
        }
        if (ex instanceof WorkflowException workflow) {
            return Mono.just(List.of(
                    GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.BAD_REQUEST)
                            .message(workflow.getMessage())
                            .build()));
        }
        if (ex instanceof IllegalArgumentException badRequest) {
            return Mono.just(List.of(
                    GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.BAD_REQUEST)
                            .message(badRequest.getMessage())
                            .build()));
        }
        if (ex instanceof IllegalStateException state) {
            return Mono.just(List.of(
                    GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.BAD_REQUEST)
                            .message(state.getMessage())
                            .build()));
        }
        if (ex instanceof DataAccessException dataAccess) {
            return Mono.just(List.of(
                    GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.BAD_REQUEST)
                            .message(sanitizeDataAccessMessage(dataAccess))
                            .build()));
        }
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getClass().getSimpleName();
        }
        return Mono.just(List.of(
                GraphqlErrorBuilder.newError(env)
                        .errorType(ErrorType.INTERNAL_ERROR)
                        .message(message)
                        .build()));
    }

    private static String sanitizeDataAccessMessage(DataAccessException ex) {
        Throwable root = ex.getMostSpecificCause();
        String message = root != null ? root.getMessage() : ex.getMessage();
        if (message == null || message.isBlank()) {
            return "database error";
        }
        if (message.contains("tenant_applications") && message.contains("does not exist")) {
            return "tenant_applications table is missing; apply database migration V015";
        }
        if (message.contains("bim_uploaded_files") && message.contains("does not exist")) {
            return "bim_uploaded_files table is missing; apply database migration V021";
        }
        return message;
    }
}
