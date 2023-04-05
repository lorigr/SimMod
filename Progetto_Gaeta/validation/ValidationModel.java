package validation;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class ValidationModel{
    
    static final int NO_EVENT = -1;
    static final int FAILED = 0;
    static final int REPAIRED = 1;
    static final int FAULTY = 2;
    static final int SERVICED = 4;
    static final int DAMAGED_REPAIR = 5;
    static final int DAMAGED_SPARE = 6;


    
    int EVENT_NUMBER = 50;

    double Z = 2500;
    double S1 = 5;
    double S2 = 50;
    double Sp1 = 4;
    double Sp2 = 200;
    double S3 = 0;
    double q10 = 0.1;
    double q12 = 0.5;
    double qs12 = 0.4;
    double qp10 = 0.9;
    double qp12 = 0.1;
    double qp23 = 0.3;
    double qp21 = 0.7;
    


    double Start;            /* Beginning of Observation Period */
    double Stop;             /* End of Observation Period */
    double ObsPeriod;        /* Length of the Observation Period */
    double clock;            /* Clock of the simulator - Simulation time */ 
    double lastclock;        /* Time of last processed event */
    double End_time;         /* Maximum simulation time */

    double inter_arrival_t;  /* inter arrival time read from trace file*/
    double arrival_t;        /* time of arrival */
    double service_t;        /* service time read from trace file*/

    double sumService = 0;      /* sum of service time */
    double sumInterarrival=0;  /* sum of the interarrival time*/
    double sumDelay = 0;
    double area = 0;
    int inService=0;
    double maxDelay = 0;
    double busy = 0;
    double doubleBusy = 0;
    double sumWaiting = 0;

    double rep_sum = 0;
    int rep = 0;


    boolean halt;            /* End of simulation flag */
    int nsys;                /* Number of customers in system */
    int nTestFix;            /* Number of customers in Test/fix station */
    int nRepair;             /* Number of customers in Repair station */
    int nSpare;              /* Number of customers in Spare-parts station */
    int nFailed;
    int nServiced;
    int nFaulty;
    int nDamaged;
    int narr;                /* Number of Arrivals */
    int ncom;                /* Number of Completions */
    int ndelayed;            /* Number of Delayed Customers */

    int event_counter;       /* Number events processed by the simulator*/
    int number_of_nodes;     /* Number of memory blocks used for the simulation */
    int return_number;       /* Number of nodes used by the simulator */
    int NMax = 20;



    int job_number;    /* (progressive) Job identification number */
    int node_number;   /* progressive number used to identify the newly generated node*/

    int N = 10;

    boolean endFlag = false;
    boolean customerInService = false;
    int nQueue = 0;
    double [] res = new double[5];

    ArrayList<Double> damaged_times = new ArrayList<Double>();
    double sum_damaged = 0.0; // somma dei tempi damaged dentro un regeneration cycle
    int damaged_count=0; // quante volte è stato raccolto il dato del tempo damaged in un ciclo
    double sum_cycle=0.0; // somm delle medie dei tempi damaged tra i vari cicli
    double double_sum_cycle =0;
    int cycle_count=0; // quanti cicli sono stati fatti

    double secondInterArrival = 0;
    double secondService = 0;
    double secondWaiting =0;
    double secondCustNum = 0;
    double secondDelay = 0;
    double doubleWaiting = 0;


    File   fp;
    Scanner scan;

    /* create a new istance of random number geneartor */
    Rngs r = new Rngs();

    /* set variable for negative exponential random variate */
    double lambda = 90;
    double mi = 15;


    /* Definition of the type used to specify the header of a queue*/
    public class dll{
        node Head;
        node Tail;
        
        public dll(){
            Head = null;
            Tail = null;
        }
    }


    /* the Future Event List, the queues for stations and the Available List are all declared as Doubly 
    * Linked Lists   with a header having fields pointing to the first 
    * and to the last elements of the list */
    dll FEL;
    dll failed;
    dll faulty;
    dll damaged;
    dll serviced;
    dll spareParts;
    dll AL;



    public class event_notice{
        int type;
        String name;
        double damaged_time;
        double occur_time;
        double arrival_time;
        double service_time;
        int previous_type;
        double repair_time;
        double spare_parts_time;
        boolean tagged = false;
    }
        
    public class node{
        event_notice event;
        node left;     // Pointer to the previous node in the doubly linked list 
        node right;  // Pointer to the next node in the doubly linked list
        
        node()
        {
            event = new event_notice();
        }
    }



    public void enqueue(node item, dll dl_list){
    /* function to add an element at the tail of a generic queue (dl_list) */    
        if(dl_list.Tail==null)
            /* curr_queue is empty*/
            dl_list.Head = item;   
        else
            /* add at the end of a non-empty dl_list */
            dl_list.Tail.right = item;
        /* adjust pointers */
        item.left = dl_list.Tail;
        item.right = null;
        dl_list.Tail = item;
        //nQueue++;
    }

    public void push_from_AL(node item){
        /* function for storing a memory block in the Available List managed as a stack */
        if(AL.Tail==null){
            AL.Tail = item;
        }else{
            AL.Head.right=item;
        }
        item.left=AL.Head;
        item.right=null;
        AL.Head=item;
    }


    public node dequeue(dll dl_list){
        /* function to remove an element at the head of a generic queue (dl_list) */
        node item;
        
        if(dl_list.Head== null)
            /* dl_list is empty */
            return null;
            
        /* point to the element being removed from dl_list */
        item = dl_list.Head;
        if(dl_list.Head.right == null){
            /* dl_list contains only one element that is being removed 
        (leaving dl_list empty) */
            dl_list.Head = null;
            dl_list.Tail = null;
        } 
        else{
            /* adjust pointers to the new head of dl_list */
            dl_list.Head = dl_list.Head.right;
            dl_list.Head.left = null;
            
        }
        /* clear the returned node*/
        item.left = null;
        item.right = null;
        
        //nQueue--;
        return item;
    }


    public node pop_form_AL(){
        /* function for getting a memory block from the Available List managed as a stack */
        node item;

        if(AL.Head==null){ // AL is empty
            return null;
        }

        item = AL.Head;
        if(AL.Head.left==null){ // the element i'm removing is the only element in AL
            AL.Head=null;
            AL.Tail=null;
        }else{ // adjust pointer to the new head of stack
            AL.Head = AL.Head.left;
            AL.Head.right = null;
        }

        item.left=null;
        item.right = null;

        return item;
    }


    public void simulate(int seed) {
        /* Simulation core */
        r.plantSeeds(seed); //planting the new seed
        initialize();
        while (!(halt)) 
            engine(); 
        
    }

    public void engine(){
        int event_type;
        double  oldclock;
        double  interval;
        node new_event;
        
        /* Get the first event notice from Future Event List */
        new_event = event_pop();
        
        oldclock = clock;
        /* update clock */
        clock = new_event.event.occur_time;
        interval=clock-oldclock;
        
        if(nsys>0){
            area = area + interval*nsys;
            secondCustNum = secondCustNum + Math.pow(nsys, 2)*interval;
            busy = busy+interval;
        }

        /* Identify and process current event */
        event_type = new_event.event.type;
        switch(event_type){
            case FAILED : test_fix(new_event); 
            break;
            case REPAIRED : repaired(new_event); // sarebbe la departure di prima
            break;
            case SERVICED : test_fix(new_event); 
            break;
            case FAULTY : repair(new_event); 
            break;
            case DAMAGED_REPAIR : repair(new_event);
            break;
            case DAMAGED_SPARE : spare_parts(new_event);
            break; 
            }
        event_counter++;
        lastclock = clock;
    }

    public void nextTextFix(){
        if(nFailed>0){ // la precedenza è sui failed 
            //nFailed--;
            //nTestFix--;
            node next_job = dequeue(failed);
            exit_failed(next_job);
        }else if(nServiced>0){
            //nServiced--;
            //nTestFix--;
            node next_job =dequeue(serviced);
            exit_serviced(next_job);
        }
    }

    public void nextServiced(){
        if(nServiced>0){

            node next_job =dequeue(serviced);
            exit_serviced(next_job);
        }
    }

    public void nextFailed(){
        if(nFailed>0){ 
            node next_job = dequeue(failed);
            exit_failed(next_job);
        }
    }

    public void nextRepair(){
        if(nFaulty>0){ // priorità ai faulty
            //nFaulty--;
            //nRepair--;
            node next_job = dequeue(faulty);
            exit_faulty(next_job);
        }else if(nDamaged>0){
            //nDamaged--;
            //nRepair--;
            node next_job = dequeue(damaged);
            exit_damaged(next_job);
        }
    }

    public void nextDamaged(){
        if(nDamaged>0){
            //nDamaged--;
            //nRepair--;
            node next_job = dequeue(damaged);
            exit_damaged(next_job);
        }
    }

    public void nextFaulty(){
        if(nFaulty>0){ 
            //nFaulty--;
            //nRepair--;
            node next_job = dequeue(faulty);
            exit_faulty(next_job);
        }
    }

    public void repaired(node item){
        if(item.event.previous_type==FAILED || item.event.previous_type==SERVICED){
            nTestFix--;
            if(item.event.previous_type==FAILED){
                nFailed--;
                nextFailed();
            } 
            else{
                nServiced--;
                damaged_count++;
                double damaged_tim = clock-item.event.damaged_time;
                sum_damaged+= damaged_tim;

                rep_sum += item.event.repair_time;
                nextServiced();
            } 
            //nextTextFix();
        }else if(item.event.previous_type==FAULTY){
            nRepair--;
            nFaulty--;
            //nextFaulty();
            nextRepair();
        }
        if(item.event.tagged==true){
            if(nFailed==0&&nDamaged==0){ // regeneration point founded
                if(damaged_count!=0){
                    regenerate();
                }
            }
            return_node(item);
            planNewFailure(true);
        }else{
            return_node(item);
            planNewFailure(false);
        }        
    }

    public void regenerate(){
        double left_limit, right_limit;
        double precision;
        double tstar = 1.645;
        sum_cycle += sum_damaged/damaged_count; // media del tempo da trovare in un ciclo
        double_sum_cycle += Math.pow(sum_damaged/damaged_count, 2);
        cycle_count++;

        System.out.println(rep_sum/rep);
        if(cycle_count==1) return;

        double mean_cycle, double_mean_cycle;
        mean_cycle = sum_cycle/cycle_count;
        double_mean_cycle = double_sum_cycle/cycle_count;

        double var_mean_cycle = (cycle_count/(cycle_count-1))*double_mean_cycle-Math.pow(mean_cycle, 2);
        double stDev_mean_cycle = Math.sqrt(var_mean_cycle);
        left_limit = ((mean_cycle)-((tstar*stDev_mean_cycle)/(Math.sqrt(cycle_count))));
        right_limit = ((mean_cycle)+((tstar*stDev_mean_cycle)/(Math.sqrt(cycle_count))));
        precision = (tstar*stDev_mean_cycle)/(Math.sqrt(cycle_count)*mean_cycle);
        //System.out.println("left :"+left_limit+", right: "+right_limit+", interval: "+precision);
        if(precision<=0.05){
            end(left_limit, right_limit, precision, cycle_count);
        } 
        damaged_count=0;
        sum_damaged=0;
        long newSeed = r.getSeed();
        r.plantSeeds(newSeed);
    }

    public void exit_damaged(node item){
        item.event.previous_type = DAMAGED_REPAIR;
        double test = GetBernoulli(qp23);
        if(test==1){
            //caso spare-parts
            item.event.type= DAMAGED_SPARE;
            item.event.occur_time = clock + GetNegExp(200);
            schedule(item);
        }else{
            // caso serviced
            item.event.type= SERVICED;
            item.event.occur_time = clock + GetNegExp(200);
            schedule(item);
        }
    }

    public void exit_failed(node item){ 
        item.event.previous_type = FAILED;
        double test = GetBernoulli(q10);
        if(test == 1){
            //caso repaired
            item.event.type = REPAIRED;
            item.event.occur_time = clock + GetNegExp(5);
            schedule(item);
        }else{
            test = GetBernoulli(qs12);
            if(test == 0){
                // caso faulty
                item.event.type = FAULTY;
                item.event.occur_time = clock + GetNegExp(5);
                schedule(item);
            }else{
                // caso damaged
                item.event.type = DAMAGED_REPAIR;
                item.event.occur_time = clock + GetNegExp(5);
                schedule(item);
            }
        }
    }

    public void exit_serviced(node item){
        item.event.previous_type = SERVICED;
        double test = GetBernoulli(qp10);
        if(test==1){
            // caso repaired
            item.event.type = REPAIRED;
            item.event.occur_time = clock + GetNegExp(Sp1);
            schedule(item);
        }else{
            // caso damaged
            item.event.type = DAMAGED_REPAIR;
            item.event.occur_time = clock + GetNegExp(Sp1);
            schedule(item);
        }
    }

    public void exit_faulty(node item){
        item.event.previous_type = FAULTY;
        item.event.type = REPAIRED;
        item.event.occur_time = clock + GetNegExp(S2);
        schedule(item);
    }

    public void repair(node item){
        // libero le code precendenti
        if(item.event.previous_type==FAILED || item.event.previous_type==SERVICED){
            nTestFix--;
            if(item.event.previous_type==FAILED){
                nFailed--;
                item.event.damaged_time=clock;
                nextFailed();
            } 
            else{
                nServiced--;
                nextServiced();
            } 
            //nextTextFix();
        }else if(item.event.previous_type==DAMAGED_SPARE){
            nSpare--;
            // next spare parts
            if(nSpare>0){
                //nSpare--;
                node next_job = dequeue(spareParts);
                exit_spare(next_job);
            }
        }

        //inserisco nella stazione attuale: la repair
        item.event.repair_time=clock;
        nRepair++;

        if(nRepair==1){ // empty server
            if(item.event.type==FAULTY){
                nFaulty++;
                exit_faulty(item);
            }else if(item.event.type==DAMAGED_REPAIR){
                nDamaged++;
                exit_damaged(item);
            }
        }else{ // busy server
            if(item.event.type==FAULTY){
                nFaulty++;
                item.event.occur_time = 0.0;
                enqueue(item, faulty);
            }else if(item.event.type==DAMAGED_REPAIR){
                nDamaged++;
                item.event.occur_time = 0.0;
                enqueue(item, damaged);
            }
        }

    }

    public void exit_spare(node item){
        item.event.previous_type = DAMAGED_SPARE;
        item.event.type = DAMAGED_REPAIR;
        double service = GetNegExp(S3);
        item.event.occur_time = clock + service;
        schedule(item);
    }

    public void spare_parts(node item){
        nRepair--;
        nDamaged--;
        nextRepair();

        item.event.spare_parts_time = clock;
        nSpare++;

        item.event.repair_time = clock - item.event.repair_time;
        rep++;

        if(nSpare==1){
            exit_spare(item);
        }else{
            item.event.occur_time=0.0;
            enqueue(item, spareParts);
        }
    }

    public void test_fix(node item){ 
        item.event.arrival_time = clock;

        if(item.event.previous_type==DAMAGED_REPAIR){
            nRepair--;
            nDamaged--;
            nextRepair();
        }
        nTestFix++;

        if(item.event.type==FAILED){
            nFailed++;
            if(nFailed==1){
                exit_failed(item);
            }else{
                item.event.occur_time=0.0;
                enqueue(item, failed);
            }
        }else if(item.event.type==SERVICED){
            nServiced++;
            item.event.repair_time = clock - item.event.repair_time;
            rep++;
            if(nServiced==1){
                exit_serviced(item);
            }else{
                item.event.occur_time=0.0;
                enqueue(item, serviced);
            }
        } 
    }

    public void initialize(){            
        /* Control Settings  */
        halt = false;
        
        /* Basic counters */
        job_number      = 1;
        event_counter   = 0;
        number_of_nodes = 0; 
        
        /* Future Event List */
        FEL = new dll();
        FEL.Head = null;
        FEL.Tail = null;
        /* Input Queue of Server */
        failed = new dll();
        failed.Head = null;
        failed.Tail = null;

        faulty = new dll();
        faulty.Head = null;
        failed.Tail = null;
        
        damaged = new dll();
        damaged.Head = null;
        damaged.Tail = null;
        
        serviced = new dll();
        serviced.Head = null;
        serviced.Tail = null;
        
        spareParts = new dll();
        spareParts.Head = null;
        spareParts.Tail = null;
        /* Available List */
        AL = new dll();
        AL.Head = null;
        AL.Tail = null;
        
        /* Basic Statistic Measures  */
        nsys     = 0;
        narr     = 0;
        ncom     = 0;
        ndelayed = 0;
        clock    = 0.0;
        nFailed= 0;
        nServiced = 0;
        nFaulty = 0;
        nDamaged = 0;
        nSpare = 0;
        nTestFix = 0;
        nRepair = 0;
        
        planInitialFailure();
    }

    public void planInitialFailure(){
        planNewFailure(true);
        for(int i = 0; i<NMax-1; i++){
            planNewFailure(false);
        }
    }

    public void planNewFailure(boolean tag){
        inter_arrival_t = GetInterArrival();
        arrival_t = clock + inter_arrival_t;
        service_t = GetUniform(3, 7); // potrebe non servire

        node newNode = get_new_node();
        newNode.event.name = "J" + (job_number++);   
        newNode.event.type = FAILED;   
        newNode.event.previous_type = NO_EVENT;
        newNode.event.damaged_time =0.0;   
        newNode.event.occur_time = arrival_t;   
        newNode.event.arrival_time = arrival_t;   
        newNode.event.service_time = service_t;  
        newNode.event.repair_time = 0;
        newNode.event.spare_parts_time = 0;     
        newNode.right = null;   
        newNode.left = null; 
        if(tag==true) newNode.event.tagged=true;
        schedule(newNode);
    }

    public void end(double left, double right, double precision, int count){
        /* manage END event */
        halt = true;
        System.out.println("---------------------------------------------------------------");
        System.out.println("Interval of average total repair time of damaged machines:\n");
        System.out.println("left limit: "+left+", right limit: "+right);
        System.out.println("reached precision: "+precision*100+"%, after: "+ count+" cycles");
        System.out.println("---------------------------------------------------------------");

    }

    /* =========================== */ 
    double GetInterArrival()              
    /* =========================== */
    {   
        r.selectStream(1);
        double random =r.negExp(Z);
        sumInterarrival = sumInterarrival+ random;
        secondInterArrival = Math.pow(random, 2)+secondInterArrival;
        return random;
    }

    /* =========================== */   
    double GetNegExp(double mi)                 
    /* =========================== */
    {   
        r.selectStream(2);
        double random = r.negExp(mi);
        return random;
    }

    double GetUniform(double a, double b){
        r.selectStream(3);
        double random = r.Uniform(a, b);
        return random;
    }

    double GetBernoulli(double p){
        r.selectStream(4);
        double random = r.Bernoulli(p);
        return random;
    }

    double GetHyperExp(double a, double b, double m1, double m2){
        r.selectStream(5);
        double random = r.hyperExp(a, b, m1, m2);
        return random;
    }

    public node get_new_node(){
        ValidationModel.node node;
        if(AL.Head!=null){
            node = pop_form_AL();
        }else{
            node = new ValidationModel.node();
            number_of_nodes++;
        }
        return node;
    }

    public void return_node(node item){
        /* function for clearing the pointers and the contents of a node of a list*/
        item.left=null;
        item.right=null;
        item.event.arrival_time=0;
        item.event.damaged_time=0;
        item.event.name="";
        item.event.occur_time=0;
        item.event.service_time=0;
        item.event.type=0;
        push_from_AL(item);
    }


    public void schedule(node new_node_event){
        /* function for the ordered insertion of a new node in the Future Event List (FEL).
        - The ordering key is represented by the occurrence time.
        - The head of FEL points to the node with the earliest occurr time.
        - The tail of FEL points to the node with the largest occurr time.*/


        node curr;

        if(FEL.Head==null){ // the list is empty
            FEL.Head = new_node_event;
            FEL.Tail = new_node_event;
        }else if(FEL.Head.event.occur_time >= new_node_event.event.occur_time){ // the position of the new node is the first
            new_node_event.right=FEL.Head;
            new_node_event.right.left = new_node_event;
            FEL.Head = new_node_event;
        }else{
            curr = FEL.Head;
            while(curr.right!=null && curr.right.event.occur_time < new_node_event.event.occur_time){ // find the right position of the new node
                curr = curr.right;
            }
            new_node_event.right = curr.right;

            if(curr.right!=null){
                new_node_event.right.left = new_node_event;
            }

            curr.right = new_node_event;
            new_node_event.left = curr;

            if(new_node_event.right == null){ // if the new node is in last position
                FEL.Tail = new_node_event;
            }
        }   
    }

    public node event_pop(){
        /* function to remove the first element from the Future event List (FEL)*/

        if(FEL.Head==null){
            return null;
        }

        node item;
        if(FEL.Head==FEL.Tail){
            item=FEL.Head;
            FEL.Head=null;
            FEL.Tail=null;
            
        }else{
            item = FEL.Head;
            FEL.Head = item.right;
            FEL.Head.left=null;
        }
        item.left=null;
        item.right=null;
        return item;
    }

    public void destroy_list(node p){
        p.left=null;
        p.right=null;
        p.event.arrival_time=0;
        p.event.damaged_time=0;
        p.event.name="";
        p.event.occur_time=0;
        p.event.service_time=0;
        p.event.type=0;
    }
}
