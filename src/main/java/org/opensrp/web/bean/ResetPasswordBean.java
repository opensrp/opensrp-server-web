/**
 *
 */
package org.opensrp.web.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Samuel Githengi created on 05/04/20
 */

@NoArgsConstructor
@Setter
@Getter
public class ResetPasswordBean {

    private String currentPassword;

    private String newPassword;

    private String confirmation;

}
