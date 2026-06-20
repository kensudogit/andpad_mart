package jp.andpad.imart.auth.spi;

import java.util.Collection;

/**
 * intra-mart テナント ロール認可 SPI。
 *
 * <p>intra-mart SSJS API の {@code RoleInfoManager} に相当する操作を抽象化する。
 * ログインユーザが指定ロールを「内包」しているかを判定する。
 *
 * <p>実装:
 * <ul>
 *   <li>{@link jp.andpad.imart.auth.stub.DevIntraMartRoleService} — ローカルスタブ</li>
 *   <li>{@link jp.andpad.imart.auth.http.HttpIntraMartRoleService} — HTTP ブリッジ</li>
 * </ul>
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/tenant/RoleInfoManager/index.html">RoleInfoManager</a>
 */
public interface IntraMartRoleService {

    /**
     * ログインユーザが指定ロールを内包しているか判定する。
     *
     * <p>IM API 相当: {@code RoleInfoManager.certify(roleId)}
     *
     * @param sessionId IM セッション ID（ログインユーザの特定に使用）
     * @param roleId    判定対象ロール ID
     * @return 内包していれば {@code true}
     */
    boolean certify(String sessionId, String roleId);

    /**
     * ログインユーザが指定ロールのいずれかを内包しているか判定する。
     *
     * @param sessionId IM セッション ID
     * @param roleIds   判定対象ロール ID 集合
     * @return いずれかを内包していれば {@code true}
     */
    boolean certifyAny(String sessionId, Collection<String> roleIds);

    /**
     * ログインユーザが内包するロール ID 一覧を取得する。
     *
     * @param sessionId IM セッション ID
     * @return ロール ID 一覧（未ログイン時は空）
     */
    Collection<String> listRoleIds(String sessionId);
}
