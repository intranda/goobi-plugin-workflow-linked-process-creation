<provenanceentry>
    <div class="provenance-entry">
        Provenienz: {props.value.type}
        <div class="action">
            <a onclick={deleteProvenance}>
              <span class="fa fa-minus-circle" />
            </a>
        </div>

    </div>
    <div class="row" each={key in Object.keys(props.value.values)} if={key != 'type'}>
        <div class="col-12 col-md-3">
            {key}
        </div>
        <div class="col-12 col-md-9">
            <input class="form-control" disabled value={props.value.values[key]}></input>
        </div>
    </div>

    <style>
        .form-control[disabled] {
            background-color: #fafafa
        }
        .provenance-entry {
            display: flex;
            width: 100%;
            padding: 10px;
            background-color: #f4f4f4;
            color: #555;
            text-decoration: underline;
            border-bottom: 1px solid #ddd;
        }
        .action {
            position: relative;
        }
        .action a {
            font-size: 16px;
            color: #777;
            cursor: pointer;
            position: absolute;
            top: -2px;
            left: 8px;
        }
        .action a:hover {
            color: #9E9E9E;
        }
    </style>

    <script>
    	export default {
    		onBeforeMount() {
    			console.log(this.props)
    		},
    		deleteProvenance() {
    			this.props.deleteValue();
    		},
    		msg(key) {
				return this.props.msg(key);
			}
    	}
    </script>

</provenanceentry>