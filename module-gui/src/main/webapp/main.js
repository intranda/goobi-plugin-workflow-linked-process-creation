import * as riot from 'riot'
import App from './tags/app.tag'

const mountApp = riot.component(App)

/* The goobiOpts look like this:
var options = {
    userId: #{LoginForm.myBenutzer.id}
};
*/
const plugin_name = window["plugin_name"];
const goobi_opts = window[plugin_name];

const app = mountApp(
    document.getElementById("root"),
    { 
        plugin_name: plugin_name,
        goobi_opts: goobi_opts
    }
)