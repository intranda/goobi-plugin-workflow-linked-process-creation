<box>
	<div class="box box--action box--no-padding form-box mb-2">
		<div class="box__title">
			<h2>
				{props.box.name}
			</h2>
		</div>
		<div class="box__content">
			<div class="box-content-top" if={ state.filteredFields.length > 0}>
				<div class="inner-addon right-addon" if={props.box.fields.filter( field => !field.show ).length > 7}>
					<span class="fa fa-search" />
					<input type="text" class="form-control" onkeyup={filter} placeholder="Filter">
					</input>
				</div>
				<a class="badge badge-intranda-light" each={field in state.filteredFields} onclick={ () => showField(field)}>
					<span class="fa fa-plus-circle" />
					{field.name}
				</a>
			</div>
			<div class="field-detail" each={field in props.box.fields} if={field.show && field.type != "MODAL_PROVENANCE"}>
				<div class="field-label">
					<div class="label-text">
						{field.name}
					</div>
					<div class="action">
						<a onclick={ () => emptyField(field)} if={!field.repeatable}>
							<span class="fa fa-minus-circle" />
						</a>
                        <a
                            class="repeatable-delete"
                            if={field.repeatable}
                            each={(value, idx) in field.values}

                            onclick={ () => deleteRepeatable(field, idx)}>
                            <span class="fa fa-minus-circle" />
                        </a>
					</div>
				</div>
				<div class="value">
					<Fieldvalue field={field} vocabularies={props.vocabularies}></Fieldvalue>
				</div>
			</div>
            <template each={(field, idx) in props.box.fields} if={field.show && field.type == "MODAL_PROVENANCE"}>
                <Provenanceentry
                    each={value in field.values}
                    field={field}
                    value={value.groupValue}
                    msg={props.msg}
                    deleteValue={getDeleteValueFromFieldFunction(field, idx)} />
            </template>
		</div>
	</div>
    <Provenancemodal
        if={state.showProvenanceModal}
        hide={hideProvenanceModal}
        field={state.provenanceField}
        vocabularies={props.vocabularies}
        msg={props.msg}
        valuesChanged={valuesChanged} />

	<style>
		.box-title {
			color: white;
		}
		.box .box-title h3 {
			margin-left: 10px;
		}
		.box-content-top {
			padding: 10px;
			background-color: #f4f4f4;
			border-bottom: 1px solid #ddd;
		}
		.box-content-top .badge {
			margin-right: 5px;
			margin-top: 5px;
		}
		.box-content-top .badge:hover {
			color: white;
			background-color: #9E9E9E;
		}
		.box-content-top .badge .fa {
			margin-right: 3px;
		}
		.field-detail {
			display: flex;
			line-height: 24px;
			border-bottom: 1px solid #ddd;
		}
		.field-detail .field-label {
			display: flex;
			flex-basis: 40%;
			padding: 10px;
			background-color: #f4f4f4;
			color: #555;
		}
		.field-detail .field-label .label-text {
			flex-grow: 1;
		}
		.field-detail .field-label .action a {
			font-size: 16px;
			color: #777;
			cursor: pointer;
		}
		.field-detail .field-label .action a:hover {
			color: #9E9E9E;
		}
		.field-detail .value {
			padding: 10px;
			flex-grow: 1;
            max-width: 60%;
		}
		.inner-addon {
		    position: relative;
		    margin-bottom: 10px;
		}
		.inner-addon .fa {
		    position: absolute;
		    padding: 5px 10px 5px 5px;
		    pointer-events: none;
		}
		.right-addon .fa {
			right: 0px;
		}
        .action .repeatable-delete:not(:first-child) {
            display: block;
            margin-top: 5px;
        }
	</style>

	<script>
	import Fieldvalue from './fieldvalue.tag';
	import Provenancemodal from './provenancemodal.tag';
	import Provenanceentry from './provenanceentry.tag';
	export default {
	    components: {
			Fieldvalue,
			Provenancemodal,
			Provenanceentry
	    },
	    onBeforeMount(state, props) {
	        this.state = {
	        	filteredFields: [],
	        	search: '',
	        	showProvenanceModal: false,
	        	mounted: false
	        }
	    },
	    onMounted(state, props) {
	        this.filterFields();
	    },
	    onBeforeUpdate() {
	    	this.emptyFilter();
	    },
	    hideProvenanceModal() {
	    	this.state.showProvenanceModal = false
	    	this.update();
	    },
	    showField(field) {
	        field.show = true;
	        if(field.multiVocabulary) {
	        	this.state.showProvenanceModal = true;
	        	this.state.provenanceField = field;
	        }
	        if(field.repeatable && field.show) {
	        	console.log(field)
				field.values.push({value: "", id: field.values.length})
	        }
	        this.filterFields();
	        this.update();
	    },
	    emptyField(field) {
	        field.show = false;
	        field.values = [];
	        this.filterFields();
	    },
	    deleteRepeatable(field, idx) {
	    	console.log("deleteRepeatable", idx, field.values)
	    	field.values.splice(idx, 1);
	    	let values = field.values;
	    	field.values = [];
	    	this.update();
	    	field.values = values;
	    	this.update();
	    	console.log(field.values)
	    	if(field.values.length == 0) {
	    		this.emptyField(field);
	    	}
	    },
	    filter(e) {
	        this.state.search = e.target.value.toLowerCase();
	        this.filterFields();
	    },
	    emptyFilter() {
	    	this.state.filteredFields = this.props.box.fields.filter(field => !field.show || field.repeatable && !field.copy);
	    },
	    filterFields() {
	    	if(this.state.search == '') {
	            this.emptyFilter();
	            this.update()
	            return;
	        }
	        this.state.filteredFields = this.props.box.fields
	        	.filter(field => field.name.toLowerCase().indexOf(this.state.search) >= 0 && !field.show);
	        this.update();
	    },
	    valuesChanged() {
	    	this.update();
	    },
	    getDeleteValueFromFieldFunction(field, valueIndex) {
	    	return () => {
	    		field.values.splice(valueIndex, 1);
	    		this.update();
	    	}
	    }
	}

	</script>
</box>