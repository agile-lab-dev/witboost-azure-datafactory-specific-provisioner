package it.agilelab.witboost.datafactory.bean;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellConfig;
import it.agilelab.witboost.datafactory.config.PowerShellBeanConfig;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PowerShellBean {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PowerShell powershell(PowerShellBeanConfig config) {
        var ps = PowerShell.openSession();
        return ps.configuration(new PowerShellConfig(config.waitPause(), config.maxWait(), config.tempFolder()));
    }
}
