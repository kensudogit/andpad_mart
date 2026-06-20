package jp.andpad.imart.auth.spi;

import java.util.Collection;

/**
 * intra-mart テナント {@code RoleInfoManager} 相当の SPI。
 *
 * @see <a href="https://api.intra-mart.jp/iap/apilist-ssjs/doc/tenant/RoleInfoManager/index.html">RoleInfoManager</a>
 */
public interface IntraMartRoleService {

    /**
     * ログインユーザが指定ロールを内包しているか（{@code RoleInfoManager.certify}）。
     */
    boolean certify(String sessionId, String roleId);

    /** いずれかのロールを内包しているか。 */
    boolean certifyAny(String sessionId, Collection<String> roleIds);

    /** 指定ロール ID 一覧を取得する。 */
    Collection<String> listRoleIds(String sessionId);
}
