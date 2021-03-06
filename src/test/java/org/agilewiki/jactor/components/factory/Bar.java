package org.agilewiki.jactor.components.factory;

import org.agilewiki.jactor.ResponseProcessor;
import org.agilewiki.jactor.bind.JBActor;
import org.agilewiki.jactor.bind.MethodBinding;
import org.agilewiki.jactor.components.Component;
import org.agilewiki.jactor.components.Include;
import org.agilewiki.jactor.components.actorName.ActorName;
import org.agilewiki.jactor.components.actorName.GetActorName;

import java.util.ArrayList;

public class Bar extends Component {
    private String myName;
    
    @Override
    public ArrayList<Include> includes() {
        ArrayList<Include> rv = new ArrayList<Include>();
        rv.add(new Include(ActorName.class));
        return rv;
    }

    @Override
    public void open(JBActor.Internals internals, final ResponseProcessor rp) throws Exception {
        super.open(internals, new ResponseProcessor() {
            @Override
            public void process(Object response) throws Exception {
                bind(Hi.class.getName(), new MethodBinding() {
                    @Override
                    protected void processRequest(Object request, final ResponseProcessor rp1) throws Exception {
                        send(getActor(), new GetActorName(), new ResponseProcessor() {
                            @Override
                            public void process(Object response) throws Exception {
                                myName = (String) response;
                                System.err.println("Hello world! --" + myName);
                                rp1.process(null);
                            }
                        });
                    }
                });

                rp.process(null);
            }
        });
    }

    @Override
    public void close() throws Exception {
        System.err.println("Bye! --" + myName);
    }
}
