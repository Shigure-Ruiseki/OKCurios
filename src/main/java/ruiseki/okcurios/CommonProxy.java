package ruiseki.okcurios;

import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.CommonProxyComponent;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKCurios.instance;
    }
}
