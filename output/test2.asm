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
        DataZ        8                         
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    
        PushI        1                         
        PushI        3                         
        Duplicate                              
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Exchange                               
        Duplicate                              
        Memtop                                 
        PushI        8                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Label        -rational-1-start         
        Duplicate                              
        Memtop                                 
        PushI        16                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -rational-1-false         
        Exchange                               
        Memtop                                 
        PushI        16                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -rational-1-start         
        Label        -rational-1-false         
        Pop                                    
        Duplicate                              
        Memtop                                 
        PushI        12                        
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
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Divide                                 
        PushI        1                         
        PushI        6                         
        Duplicate                              
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Exchange                               
        Duplicate                              
        Memtop                                 
        PushI        8                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Label        -rational-2-start         
        Duplicate                              
        Memtop                                 
        PushI        16                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -rational-2-false         
        Exchange                               
        Memtop                                 
        PushI        16                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -rational-2-start         
        Label        -rational-2-false         
        Pop                                    
        Duplicate                              
        Memtop                                 
        PushI        12                        
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
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Divide                                 
        Exchange                               
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        Memtop                                 
        PushI        8                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Exchange                               
        Duplicate                              
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Multiply                               
        Memtop                                 
        PushI        16                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Label        -rational-3-start         
        Duplicate                              
        Memtop                                 
        PushI        20                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -rational-3-false         
        Exchange                               
        Memtop                                 
        PushI        20                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -rational-3-start         
        Label        -rational-3-false         
        Pop                                    
        Memtop                                 
        PushI        16                        
        Subtract                               
        LoadI                                  
        Exchange                               
        Divide                                 
        Duplicate                              
        Memtop                                 
        PushI        20                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Divide                                 
        Multiply                               
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        20                        
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Divide                                 
        Multiply                               
        Subtract                               
        Duplicate                              
        Memtop                                 
        PushI        24                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        20                        
        Subtract                               
        LoadI                                  
        Label        -rational-3-start2        
        Duplicate                              
        Memtop                                 
        PushI        28                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -rational-3-false2        
        Exchange                               
        Memtop                                 
        PushI        28                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -rational-3-start2        
        Label        -rational-3-false2        
        Pop                                    
        Duplicate                              
        Memtop                                 
        PushI        28                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        24                        
        Subtract                               
        LoadI                                  
        Exchange                               
        Divide                                 
        Memtop                                 
        PushI        20                        
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        28                        
        Subtract                               
        LoadI                                  
        Divide                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    
        Exchange                               
        StoreI                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    
        LoadI                                  
        Divide                                 
        Duplicate                              
        JumpFalse    -compare-4-false          
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-4-join           
        Label        -compare-4-false          
        Pop                                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    
        LoadI                                  
        Multiply                               
        JumpNeg      -compare-4-neg            
        Jump         -compare-4-join           
        Label        -compare-4-neg            
        PushI        45                        
        PushD        $print-format-character   
        Printf                                 
        Label        -compare-4-join           
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    
        LoadI                                  
        Remainder                              
        Duplicate                              
        JumpFalse    -compare-4-false2         
        PushI        95                        
        PushD        $print-format-character   
        Printf                                 
        Duplicate                              
        JumpNeg      -compare-4-true           
        Jump         -compare-4-join2          
        Label        -compare-4-true           
        Negate                                 
        Label        -compare-4-join2          
        PushD        $print-format-integer     
        Printf                                 
        PushI        47                        
        PushD        $print-format-character   
        Printf                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    
        LoadI                                  
        Duplicate                              
        JumpPos      -compare-4-pos            
        Negate                                 
        Label        -compare-4-pos            
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-4-join3          
        Label        -compare-4-false2         
        Pop                                    
        Label        -compare-4-join3          
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
