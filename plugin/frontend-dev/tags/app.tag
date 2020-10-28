<app>
	<div class="row">
		<div class="col-md-6" each={column in state.currentScreen.columns}>
			<Box each={box in column.boxes} box={box} vocabularies={state.vocabularies} msg={msg}></Box>
		</div>
	</div>  
	<div class="row" style="margin-top: 15px; margin-bottom: 20px;">
        <div class="col-md-12">
            <hr></hr>
        </div>
		<div class="col-md-2">
            <select class="form-control" onchange={changeCurrentScreen}>
                <option each={s in state.screens}>{s.name}</option>
            </select>
		</div>
		<div class="col-md-10">
			<div class="pull-right">
				<button class="btn" onclick={save}>{msg('cancel')}</button>
				<button class="btn btn-success" style="margin-left: 15px;" onclick={saveAndExit}><i class="fa-btn fa fa-floppy-o"></i>{msg('create_process')}</button>
			</div>
		</div>
	</div>
	
	<style>
	 .btn .fa-btn {
	 	margin-right: 5px;
	 }
	</style>
  
  <script>
  import Box from './box.tag';
  import Preview from './preview.tag';
  import Imagemodal from './imagemodal.tag';
  export default {
    components: {
      Box,
      Preview,
      Imagemodal
    },
    onBeforeMount(props, state) {
      this.state = {
    	  msgs: {},
          vocabularies: {},
          currentScreen: 0,
          screens: []
      };
      fetch(`/goobi/plugins/processcreation/allCreationScreens`).then(resp => {
  		resp.json().then(json => {
  			this.state.screens = json;
  			console.log(this.state.screens)
  			this.state.currentScreen = this.state.screens[0];
			this.update();
  		});
      });
      fetch(`/goobi/plugins/processcreation/vocabularies`).then(resp => {
		resp.json().then(json => {
			this.state.vocabularies = json;
			console.log(this.state.vocabularies)
			this.update();
		});
      });
      fetch(`/goobi/api/messages/${props.goobi_opts.language}`, {
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
    			this.update();
    			break;
    		}
    	}
    	this.update();
    },
    save() {
    	fetch(`/goobi/plugins/ce/process/${this.props.goobi_opts.processId}/mets`, {
    		method: "POST",
    		body: JSON.stringify(this.state.boxes)
    	}).catch(err => {
    		alert("There was an error saving your data");
    	})
    },
    saveAndExit() {
    	fetch(`/goobi/plugins/ce/process/${this.props.goobi_opts.processId}/mets`, {
    		method: "POST",
    		body: JSON.stringify(this.state.boxes)
    	}).then( r => {
    		this.leavePlugin();
    	}).catch(err => {
    		alert("There was an error saving your data");
    	})
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
    	document.querySelector('#restPluginFinishLink').click();
    }
  }
  </script>
</app>
