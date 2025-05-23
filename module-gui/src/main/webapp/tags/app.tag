<app>
	<div class="flow">
		<div class="row">
			<div class="col-md-6" each={column in state.currentScreen.columns}>
				<Box each={box in column.boxes} box={box} vocabularies={state.vocabularies} msg={msg}></Box>
			</div>
		</div>
		<div class="d-flex flex-nowrap justify-content-between">
			<div class="form-horizontal">
				<div class="row">
					<label class="col-sm-4 control-label" for="templateSelect">Vorlage auswählen:</label>
					<div class="col-sm-8">
						<select class="form-select" onchange={changeCurrentScreen} id="templateSelect">
							<option each={s in state.screens}>{s.name}</option>
						</select>
					</div>
				</div>
			</div>
			<div>
				<button class="btn btn-blank" onclick={save}>{msg('cancel')}</button>
				<button class="btn btn-success" style="margin-left: 15px;" onclick={saveAndExit}>
					<span class="fa fa-floppy-o" />
					<span>{msg('create_process')}</span>
				</button>
			</div>
		</div>
	</div>

	<style>
	 .btn .fa-btn {
	 	margin-right: 5px;
	 }
     .inline-label {
        display: inline;
     }
	</style>

  <script>
  import Box from './box.tag';
  import Preview from './preview.tag';
  import Imagemodal from './imagemodal.tag';

  const goobi_path = location.pathname.split('/')[1];
  export default {
    components: {
      Box,
      Preview,
      Imagemodal
    },
    onBeforeMount(props, state) {
    	console.log(props)
      this.state = {
    	  msgs: {},
          vocabularies: {},
          currentScreen: {},
          screens: [],
          pluginName: props.plugin_name
      };
      fetch(`/${goobi_path}/api/plugins/processcreation/allCreationScreens`).then(resp => {
  		resp.json().then(json => {
  			this.state.screens = json;
  			console.log(this.state.screens)
  			this.state.currentScreen = this.state.screens[0];
			this.update();
  		});
      });
      fetch(`/${goobi_path}/api/plugins/processcreation/vocabularies`).then(resp => {
		resp.json().then(json => {
			this.state.vocabularies = json;
			console.log(this.state.vocabularies)
			this.update();
		});
      });
      fetch(`/${goobi_path}/api/api/messages/${props.goobi_opts.language}`, {
          method: 'GET',
          credentials: 'same-origin'
      }).then(resp => {
        resp.json().then(json => {
          this.state.msgs = json;
          this.update();
        });
      });
    },
    onMounted(props, state) {
    },
    onBeforeUpdate(props, state) {
    },
    onUpdated(props, state) {
    	this.updateTitleAndBreadcrumb();
    },
    printState() {
    },
    changeCurrentScreen(e) {
    	this.state.currentScreen = {}
    	this.update();
    	for(var i=0; i<e.target.options.length;i++) {
    		if(e.target.options[i].selected) {
    			this.state.currentScreen = {...this.state.screens[i]};
    			console.log(this.state.currentScreen)
    			this.updateTitleAndBreadcrumb()
    			this.update();
    			break;
    		}
    	}
    	this.update();
    },
    saveAndExit() {
    	fetch(`/${goobi_path}/api/plugins/processcreation/processes`, {
    		method: "POST",
    		body: JSON.stringify(this.state.currentScreen)
    	}).then( r => {
  			alert("Prozesse erzeugt!")
    		fetch(`/${goobi_path}/api/plugins/processcreation/allCreationScreens`).then(resp => {
    	  		resp.json().then(json => {
    	  			this.state.screens = json;
    	  			console.log(this.state.screens)
    	  			this.state.currentScreen = {};
    	  			this.update();
    	  			this.state.currentScreen = this.state.screens[0];
    				this.update();
    	  		});
    	      });
    	}).catch(err => {
    		alert("There was an error saving your data");
    	})
    },
    updateTitleAndBreadcrumb() {
    	let title = this.msg(this.state.pluginName) + ": " + this.state.currentScreen.name;
    	let titleNode = document.querySelector(".page-header h1");
    	titleNode.innerText = title;
    	let breadcrumbNode = document.querySelector("#breadcrumb\\:admin\\:link span");
    	breadcrumbNode.innerText = title;
    },
    msg(str) {
      if(Object.keys(this.state.msgs).length == 0) {
          return "*".repeat(str.length);
      }
      if(this.state.msgs[str]) {
        return this.state.msgs[str];
      }
      return "???" + str + "???";
    },
    showPreview() {
    	this.state.showPreview = true;
    	var previewVals = [];
    	for(var col of this.state.boxes) {
    		for(var box of col.boxes) {
    			for(var field of box.fields) {
    				if(field.show) {
						previewVals.push(field)
    				}
    			}
    		}
    	}
    	this.state.previewVals = previewVals;
    	this.update();
    },
    hidePreview() {
    	this.state.showPreview = false;
    	this.update();
    },
    showImages() {
    	this.state.showImages = true;
    	this.update();
    },
    hideImages() {
    	this.state.showImages = false;
    	this.update();
    },
    leavePlugin() {
    	//document.querySelector('#restPluginFinishLink').click();
    }
  }
  </script>
</app>
