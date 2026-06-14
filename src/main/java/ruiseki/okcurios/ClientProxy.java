package ruiseki.okcurios;

import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ClientProxyComponent;

public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return OKCurios.instance;
    }
}
