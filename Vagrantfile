# -*- mode: ruby -*-
# vi: set ft=ruby :

# Minimum Vagrant version for plugin vagrant-librarian-chef to work properly
Vagrant.require_version ">= 1.6.5"

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # All Vagrant configuration is done here. The most common configuration
  # options are documented and commented below. For a complete reference,
  # please see the online documentation at vagrantup.com.

  # Every Vagrant virtual environment requires a box to build off of.
  config.vm.box = "ubuntu/trusty64"

  config.vm.post_up_message = "###   Image started   ###
1) Run 'vagrant ssh' to access the image
2) Change to directory gong: 'cd gong'
3) Start GONG by executing 'mvn p:run -DskipTests'"

  config.librarian_chef.cheffile_dir = "vagrant/chef"
  config.vm.provision :chef_solo do |chef|
    chef.verbose_logging = true
	chef.cookbooks_path = ['vagrant/chef/cookbooks']
    chef.run_list = [
        "recipe[java::default]",
	    "recipe[maven::default]",
	    "recipe[git::default]",
	    "recipe[couchbase::server]",
	    "recipe[couchbase::buckets]"
     ]
	chef.json = {
		"java" => {
			"install_flavor" => "oracle",
			"jdk_version" => "7",
			"accept_license_agreement" => true,
			"oracle" => {
				"accept_oracle_download_terms" => true
			}
		},
		"maven" => {
			"setup_bin" => true,
			"version" => 3,
			"3" => {
				"version" => "3.1.1"
			},
			"mavenrc" => {
				"opts" => "-Xmx2G -XX:MaxPermSize=1G"
			}
		},
		"couchbase" => {
			"server" => {
				"edition" => "enterprise",
				"version" => "3.0.1",
				"password" => "polopoly"
			},
			"buckets" => {
				"cmbucket" => {
					"saslpassword" => "cmpasswd",
					"replicas" => false
				}
			}
		} 
	}
  end
  config.vm.synced_folder ".", "/home/vagrant/gong"
  config.vm.provision "file", source: "vagrant/m2/settings.xml", destination: "/home/vagrant/.m2/settings.xml", run: "always"
  config.vm.provision "file", source: "vagrant/m2/settings-security.xml", destination: "/home/vagrant/.m2/settings-security.xml", run: "always"
  config.vm.hostname = "gong"
  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 2 
    v.name = "GONG"
  end

  # Uncomment the 2 lines below this in order to get GONG to start automatically.
  #config.vm.provision "shell",
  #  inline: "echo Starting Gong;su vagrant -l -c 'cd /home/vagrant/gong/;mvn p:run -DskipTests'"

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  config.vm.network "forwarded_port", guest: 8080, host: 8080
  config.vm.network "forwarded_port", guest: 8091, host: 9090
  # config.vm.network "private_network", type: "dhcp"
  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # If true, then any SSH connections made will enable agent forwarding.
  # Default value: false
  # config.ssh.forward_agent = true

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  # config.vm.provider "virtualbox" do |vb|
  #   # Don't boot with headless mode
  #   vb.gui = true
  #
  #   # Use VBoxManage to customize the VM. For example to change memory:
  #   vb.customize ["modifyvm", :id, "--memory", "1024"]
  # end
  #
  # View the documentation for the provider you're using for more
  # information on available options.

  # Enable provisioning with CFEngine. CFEngine Community packages are
  # automatically installed. For example, configure the host as a
  # policy server and optionally a policy file to run:
  #
  # config.vm.provision "cfengine" do |cf|
  #   cf.am_policy_hub = true
  #   # cf.run_file = "motd.cf"
  # end
  #
  # You can also configure and bootstrap a client to an existing
  # policy server:
  #
  # config.vm.provision "cfengine" do |cf|
  #   cf.policy_server_address = "10.0.2.15"
  # end

  # Enable provisioning with Puppet stand alone.  Puppet manifests
  # are contained in a directory path relative to this Vagrantfile.
  # You will need to create the manifests directory and a manifest in
  # the file default.pp in the manifests_path directory.
  #
  # config.vm.provision "puppet" do |puppet|
  #   puppet.manifests_path = "manifests"
  #   puppet.manifest_file  = "default.pp"
  # end

  # Enable provisioning with chef solo, specifying a cookbooks path, roles
  # path, and data_bags path (all relative to this Vagrantfile), and adding
  # some recipes and/or roles.
  #
  # config.vm.provision "chef_solo" do |chef|
  #   chef.cookbooks_path = "../my-recipes/cookbooks"
  #   chef.roles_path = "../my-recipes/roles"
  #   chef.data_bags_path = "../my-recipes/data_bags"
  #   chef.add_recipe "mysql"
  #   chef.add_role "web"
  #
  #   # You may also specify custom JSON attributes:
  #   chef.json = { mysql_password: "foo" }
  # end

  # Enable provisioning with chef server, specifying the chef server URL,
  # and the path to the validation key (relative to this Vagrantfile).
  #
  # The Opscode Platform uses HTTPS. Substitute your organization for
  # ORGNAME in the URL and validation key.
  #
  # If you have your own Chef Server, use the appropriate URL, which may be
  # HTTP instead of HTTPS depending on your configuration. Also change the
  # validation key to validation.pem.
  #
  # config.vm.provision "chef_client" do |chef|
  #   chef.chef_server_url = "https://api.opscode.com/organizations/ORGNAME"
  #   chef.validation_key_path = "ORGNAME-validator.pem"
  # end
  #
  # If you're using the Opscode platform, your validator client is
  # ORGNAME-validator, replacing ORGNAME with your organization name.
  #
  # If you have your own Chef Server, the default validation client name is
  # chef-validator, unless you changed the configuration.
  #
  #   chef.validation_client_name = "ORGNAME-validator"

end
