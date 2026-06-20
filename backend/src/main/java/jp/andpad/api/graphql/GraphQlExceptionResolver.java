package jp.andpad.api.graphql;

import java.util.List;

import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jp.andpad.api.security.UnauthorizedException;
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
        if (ex instanceof IllegalArgumentException badRequest) {
            return Mono.just(List.of(
                    GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.BAD_REQUEST)
                            .message(badRequest.getMessage())
                            .build()));
        }
        return Mono.empty();
    }
}
