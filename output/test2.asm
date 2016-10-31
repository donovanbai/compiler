        Jump         $$main                    
        DLabel       $eat-location-zero        
        DataZ        8                         
        DLabel       $print-format-integer     
        DataC        37                        %% "%d"
        DataC        100                       
        DataC        0                         
        DLabel       $print-format-floating    
        DataC        37                        %% "%g"
        DataC        103                       
        DataC        0                         
        DLabel       $print-format-character   
        DataC        37                        %% "%c"
        DataC        99                        
        DataC        0                         
        DLabel       $print-format-string      
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-boolean     
        DataC        37                        %% "%s"
        DataC        115                       
        DataC        0                         
        DLabel       $print-format-newline     
        DataC        10                        %% "\n"
        DataC        0                         
        DLabel       $print-format-space       
        DataC        32                        %% " "
        DataC        0                         
        DLabel       $boolean-true-string      
        DataC        116                       %% "true"
        DataC        114                       
        DataC        117                       
        DataC        101                       
        DataC        0                         
        DLabel       $boolean-false-string     
        DataC        102                       %% "false"
        DataC        97                        
        DataC        108                       
        DataC        115                       
        DataC        101                       
        DataC        0                         
        DLabel       $errors-general-message   
        DataC        82                        %% "Runtime error: %s\n"
        DataC        117                       
        DataC        110                       
        DataC        116                       
        DataC        105                       
        DataC        109                       
        DataC        101                       
        DataC        32                        
        DataC        101                       
        DataC        114                       
        DataC        114                       
        DataC        111                       
        DataC        114                       
        DataC        58                        
        DataC        32                        
        DataC        37                        
        DataC        115                       
        DataC        10                        
        DataC        0                         
        Label        $$general-runtime-error   
        PushD        $errors-general-message   
        Printf                                 
        Halt                                   
        DLabel       $errors-int-divide-by-zero 
        DataC        105                       %% "integer divide by zero"
        DataC        110                       
        DataC        116                       
        DataC        101                       
        DataC        103                       
        DataC        101                       
        DataC        114                       
        DataC        32                        
        DataC        100                       
        DataC        105                       
        DataC        118                       
        DataC        105                       
        DataC        100                       
        DataC        101                       
        DataC        32                        
        DataC        98                        
        DataC        121                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$i-divide-by-zero        
        PushD        $errors-int-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        0                         
        Label        $$main                    
        PushI        10                        
        Duplicate                              
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        PushI        -4                        
        Duplicate                              
        Memtop                                 
        PushI        8                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Label        -compare-1-start          
        Duplicate                              
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -compare-1-false          
        Exchange                               
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -compare-1-start          
        Label        -compare-1-false          
        Pop                                    
        Duplicate                              
        JumpPos      -compare-1-pos            
        Negate                                 
        Label        -compare-1-pos            
        Duplicate                              
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Exchange                               
        Divide                                 
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Exchange                               
        Divide                                 
        Memtop                                 
        PushI        8                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Divide                                 
        Duplicate                              
        JumpFalse    -compare-1-false2         
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-1-join           
        Label        -compare-1-false2         
        Pop                                    
        Label        -compare-1-join           
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Remainder                              
        Duplicate                              
        JumpFalse    -compare-1-false3         
        PushI        95                        
        PushD        $print-format-character   
        Printf                                 
        Duplicate                              
        JumpNeg      -compare-1-true           
        Jump         -compare-1-join2          
        Label        -compare-1-true           
        Negate                                 
        Label        -compare-1-join2          
        PushD        $print-format-integer     
        Printf                                 
        PushI        47                        
        PushD        $print-format-character   
        Printf                                 
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Duplicate                              
        JumpPos      -compare-1-pos2           
        Negate                                 
        Label        -compare-1-pos2           
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-1-join3          
        Label        -compare-1-false3         
        Pop                                    
        Label        -compare-1-join3          
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
