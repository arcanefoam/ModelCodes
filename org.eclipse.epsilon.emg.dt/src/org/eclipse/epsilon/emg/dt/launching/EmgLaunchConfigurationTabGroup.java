package org.eclipse.epsilon.emg.dt.launching;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.epsilon.common.dt.launching.tabs.EpsilonLaunchConfigurationTabContributor;
import org.eclipse.epsilon.common.dt.launching.tabs.ParametersConfigurationTab;
import org.eclipse.epsilon.common.dt.util.LogUtil;
import org.eclipse.epsilon.emg.dt.launching.tabs.EmgSourceConfigurationTab;
import org.eclipse.epsilon.emg.dt.launching.tabs.GeneratedModelsConfigurationTab;
import org.eclipse.epsilon.epl.dt.launching.EplLaunchConfigurationTabGroup;

public class EmgLaunchConfigurationTabGroup extends
		EplLaunchConfigurationTabGroup {
	
	@Override
	public ILaunchConfigurationTab getSourceConfigurationTab() {
		return new EmgSourceConfigurationTab();
	}
	
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		
		ArrayList<ILaunchConfigurationTab> tabList = new ArrayList<ILaunchConfigurationTab>();
		for (ILaunchConfigurationTab tab : getSourceConfigurationTabs()) {
			tabList.add(tab);
		}
		
		tabList.add(new GeneratedModelsConfigurationTab());
		tabList.add(new ParametersConfigurationTab());
		
		for (ILaunchConfigurationTab tab : getOtherConfigurationTabs()) {
			tabList.add(tab);
		}
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.epsilon.eol.dt.launchConfigurationExtension");

		for (IConfigurationElement configurationElement : extensionPoint.getConfigurationElements()) {
			try {
				EpsilonLaunchConfigurationTabContributor contributor = (EpsilonLaunchConfigurationTabContributor) configurationElement.createExecutableExtension("tabContributor");
				for(ILaunchConfigurationTab tab : contributor.getTabs(this, dialog, mode)) {
					tabList.add(tab);
				}
			}
			catch (CoreException e) {
				LogUtil.log(e);
			}
		}
		tabList.add(new CommonTab());
		
		setTabs(tabList);
	}
}
