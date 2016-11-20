        Label        -mem-manager-initialize   
        DLabel       $heap-start-ptr           
        DataZ        4                         
        DLabel       $heap-after-ptr           
        DataZ        4                         
        DLabel       $heap-first-free          
        DataZ        4                         
        DLabel       $mmgr-newblock-block      
        DataZ        4                         
        DLabel       $mmgr-newblock-size       
        DataZ        4                         
        PushD        $heap-memory              
        Duplicate                              
        PushD        $heap-start-ptr           
        Exchange                               
        StoreI                                 
        PushD        $heap-after-ptr           
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
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
        DLabel       $errors-float-divide-by-zero 
        DataC        102                       %% "floating divide by zero"
        DataC        108                       
        DataC        111                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        110                       
        DataC        103                       
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
        Label        $$f-divide-by-zero        
        PushD        $errors-float-divide-by-zero 
        Jump         $$general-runtime-error   
        DLabel       $errors-zero-denominator  
        DataC        100                       %% "denominator is zero"
        DataC        101                       
        DataC        110                       
        DataC        111                       
        DataC        109                       
        DataC        105                       
        DataC        110                       
        DataC        97                        
        DataC        116                       
        DataC        111                       
        DataC        114                       
        DataC        32                        
        DataC        105                       
        DataC        115                       
        DataC        32                        
        DataC        122                       
        DataC        101                       
        DataC        114                       
        DataC        111                       
        DataC        0                         
        Label        $$zero-denominator        
        PushD        $errors-zero-denominator  
        Jump         $$general-runtime-error   
        DLabel       $errors-invalid-index     
        DataC        105                       %% "index is invalid"
        DataC        110                       
        DataC        100                       
        DataC        101                       
        DataC        120                       
        DataC        32                        
        DataC        105                       
        DataC        115                       
        DataC        32                        
        DataC        105                       
        DataC        110                       
        DataC        118                       
        DataC        97                        
        DataC        108                       
        DataC        105                       
        DataC        100                       
        DataC        0                         
        Label        $$invalid-index           
        PushD        $errors-invalid-index     
        Jump         $$general-runtime-error   
        DLabel       $errors-negative-length   
        DataC        97                        %% "array length is negative"
        DataC        114                       
        DataC        114                       
        DataC        97                        
        DataC        121                       
        DataC        32                        
        DataC        108                       
        DataC        101                       
        DataC        110                       
        DataC        103                       
        DataC        116                       
        DataC        104                       
        DataC        32                        
        DataC        105                       
        DataC        115                       
        DataC        32                        
        DataC        110                       
        DataC        101                       
        DataC        103                       
        DataC        97                        
        DataC        116                       
        DataC        105                       
        DataC        118                       
        DataC        101                       
        DataC        0                         
        Label        $$negative-length         
        PushD        $errors-negative-length   
        Jump         $$general-runtime-error   
        DLabel       $usable-memory-start      
        DLabel       $global-memory-block      
        DataZ        8                         
        Label        $$main                    
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% a
        PushI        40                        
        Call         -mem-manager-allocate     
        Duplicate                              
        PushI        7                         
        StoreI                                 
        Duplicate                              
        PushI        4                         
        Add                                    
        PushI        0                         
        StoreC                                 
        Duplicate                              
        PushI        5                         
        Add                                    
        PushI        0                         
        StoreC                                 
        Duplicate                              
        PushI        6                         
        Add                                    
        PushI        0                         
        StoreC                                 
        Duplicate                              
        PushI        7                         
        Add                                    
        PushI        0                         
        StoreC                                 
        Duplicate                              
        PushI        8                         
        Add                                    
        PushI        8                         
        StoreI                                 
        Duplicate                              
        PushI        12                        
        Add                                    
        PushI        3                         
        StoreI                                 
        Duplicate                              
        PushI        16                        
        Add                                    
        PushI        1                         
        PushI        2                         
        Duplicate                              
        JumpFalse    $$zero-denominator        
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
        Label        -gcd-1-start              
        Duplicate                              
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -gcd-1-false              
        Exchange                               
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -gcd-1-start              
        Label        -gcd-1-false              
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
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        StoreI                                 
        Duplicate                              
        PushI        20                        
        Add                                    
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        StoreI                                 
        Duplicate                              
        PushI        24                        
        Add                                    
        PushI        1                         
        PushI        3                         
        Duplicate                              
        JumpFalse    $$zero-denominator        
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
        Label        -gcd-2-start              
        Duplicate                              
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -gcd-2-false              
        Exchange                               
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -gcd-2-start              
        Label        -gcd-2-false              
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
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        StoreI                                 
        Duplicate                              
        PushI        28                        
        Add                                    
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        StoreI                                 
        Duplicate                              
        PushI        32                        
        Add                                    
        PushI        1                         
        PushI        4                         
        Duplicate                              
        JumpFalse    $$zero-denominator        
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
        Label        -gcd-3-start              
        Duplicate                              
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -gcd-3-false              
        Exchange                               
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -gcd-3-start              
        Label        -gcd-3-false              
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
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        StoreI                                 
        Duplicate                              
        PushI        36                        
        Add                                    
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        StoreI                                 
        StoreI                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% b
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% a
        LoadI                                  
        Duplicate                              
        Duplicate                              
        PushI        8                         
        Add                                    
        LoadI                                  
        Duplicate                              
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Exchange                               
        PushI        12                        
        Add                                    
        LoadI                                  
        Duplicate                              
        Memtop                                 
        PushI        8                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Multiply                               
        PushI        16                        
        Add                                    
        Call         -mem-manager-allocate     
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Exchange                               
        StoreI                                 
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadC                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        PushI        4                         
        Add                                    
        Exchange                               
        StoreC                                 
        Duplicate                              
        PushI        5                         
        Add                                    
        LoadC                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        PushI        5                         
        Add                                    
        Exchange                               
        StoreC                                 
        Duplicate                              
        PushI        6                         
        Add                                    
        LoadC                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        PushI        6                         
        Add                                    
        Exchange                               
        StoreC                                 
        Duplicate                              
        PushI        7                         
        Add                                    
        LoadC                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        PushI        7                         
        Add                                    
        Exchange                               
        StoreC                                 
        Duplicate                              
        PushI        8                         
        Add                                    
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreI                                 
        Duplicate                              
        PushI        12                        
        Add                                    
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        PushI        12                        
        Add                                    
        Exchange                               
        StoreI                                 
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        PushI        1                         
        Subtract                               
        Exchange                               
        Memtop                                 
        PushI        16                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Label        -clone-4-start            
        Duplicate                              
        Duplicate                              
        JumpNeg      -clone-4-join             
        Memtop                                 
        PushI        16                        
        Subtract                               
        LoadI                                  
        Exchange                               
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Multiply                               
        PushI        16                        
        Add                                    
        Duplicate                              
        Memtop                                 
        PushI        20                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Add                                    
        Duplicate                              
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        20                        
        Subtract                               
        LoadI                                  
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        4                         
        Add                                    
        LoadI                                  
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        20                        
        Subtract                               
        LoadI                                  
        Add                                    
        PushI        4                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        1                         
        Subtract                               
        Jump         -clone-4-start            
        Label        -clone-4-join             
        Pop                                    
        Pop                                    
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% a
        LoadI                                  
        PushI        0                         
        Duplicate                              
        JumpNeg      $$invalid-index           
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        PushI        12                        
        Add                                    
        LoadI                                  
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Subtract                               
        Duplicate                              
        JumpFalse    $$invalid-index           
        JumpNeg      $$invalid-index           
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        PushI        8                         
        Multiply                               
        Add                                    
        PushI        16                        
        Add                                    
        PushI        1                         
        PushI        5                         
        Duplicate                              
        JumpFalse    $$zero-denominator        
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
        Label        -gcd-5-start              
        Duplicate                              
        Memtop                                 
        PushI        12                        
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        JumpFalse    -gcd-5-false              
        Exchange                               
        Memtop                                 
        PushI        12                        
        Subtract                               
        LoadI                                  
        Remainder                              
        Jump         -gcd-5-start              
        Label        -gcd-5-false              
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
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Exchange                               
        Duplicate                              
        PushI        4                         
        Add                                    
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        StoreI                                 
        Exchange                               
        StoreI                                 
        PushD        $global-memory-block      
        PushI        0                         
        Add                                    %% a
        LoadI                                  
        PushI        0                         
        Duplicate                              
        JumpNeg      $$invalid-index           
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        PushI        12                        
        Add                                    
        LoadI                                  
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Subtract                               
        Duplicate                              
        JumpFalse    $$invalid-index           
        JumpNeg      $$invalid-index           
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        PushI        8                         
        Multiply                               
        Add                                    
        PushI        16                        
        Add                                    
        Duplicate                              
        LoadI                                  
        Exchange                               
        PushI        4                         
        Add                                    
        LoadI                                  
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
        Exchange                               
        Divide                                 
        Duplicate                              
        JumpFalse    -compare-6-false2         
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-6-join           
        Label        -compare-6-false2         
        Pop                                    
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Multiply                               
        JumpNeg      -compare-6-neg            
        Jump         -compare-6-join           
        Label        -compare-6-neg            
        PushI        45                        
        PushD        $print-format-character   
        Printf                                 
        Label        -compare-6-join           
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Remainder                              
        Duplicate                              
        JumpFalse    -compare-6-false3         
        PushI        95                        
        PushD        $print-format-character   
        Printf                                 
        Duplicate                              
        JumpNeg      -compare-6-true           
        Jump         -compare-6-join2          
        Label        -compare-6-true           
        Negate                                 
        Label        -compare-6-join2          
        PushD        $print-format-integer     
        Printf                                 
        PushI        47                        
        PushD        $print-format-character   
        Printf                                 
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Duplicate                              
        JumpPos      -compare-6-pos2           
        Negate                                 
        Label        -compare-6-pos2           
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-6-join3          
        Label        -compare-6-false3         
        Pop                                    
        Label        -compare-6-join3          
        PushD        $print-format-newline     
        Printf                                 
        PushD        $global-memory-block      
        PushI        4                         
        Add                                    %% b
        LoadI                                  
        PushI        0                         
        Duplicate                              
        JumpNeg      $$invalid-index           
        Memtop                                 
        PushI        4                         
        Subtract                               
        Exchange                               
        StoreI                                 
        Duplicate                              
        PushI        12                        
        Add                                    
        LoadI                                  
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Subtract                               
        Duplicate                              
        JumpFalse    $$invalid-index           
        JumpNeg      $$invalid-index           
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        PushI        8                         
        Multiply                               
        Add                                    
        PushI        16                        
        Add                                    
        Duplicate                              
        LoadI                                  
        Exchange                               
        PushI        4                         
        Add                                    
        LoadI                                  
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
        Exchange                               
        Divide                                 
        Duplicate                              
        JumpFalse    -compare-7-false2         
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-7-join           
        Label        -compare-7-false2         
        Pop                                    
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Multiply                               
        JumpNeg      -compare-7-neg            
        Jump         -compare-7-join           
        Label        -compare-7-neg            
        PushI        45                        
        PushD        $print-format-character   
        Printf                                 
        Label        -compare-7-join           
        Memtop                                 
        PushI        8                         
        Subtract                               
        LoadI                                  
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Remainder                              
        Duplicate                              
        JumpFalse    -compare-7-false3         
        PushI        95                        
        PushD        $print-format-character   
        Printf                                 
        Duplicate                              
        JumpNeg      -compare-7-true           
        Jump         -compare-7-join2          
        Label        -compare-7-true           
        Negate                                 
        Label        -compare-7-join2          
        PushD        $print-format-integer     
        Printf                                 
        PushI        47                        
        PushD        $print-format-character   
        Printf                                 
        Memtop                                 
        PushI        4                         
        Subtract                               
        LoadI                                  
        Duplicate                              
        JumpPos      -compare-7-pos2           
        Negate                                 
        Label        -compare-7-pos2           
        PushD        $print-format-integer     
        Printf                                 
        Jump         -compare-7-join3          
        Label        -compare-7-false3         
        Pop                                    
        Label        -compare-7-join3          
        PushD        $print-format-newline     
        Printf                                 
        Halt                                   
        Label        -mem-manager-make-tags    
        DLabel       $mmgr-tags-size           
        DataZ        4                         
        DLabel       $mmgr-tags-start          
        DataZ        4                         
        DLabel       $mmgr-tags-available      
        DataZ        4                         
        DLabel       $mmgr-tags-nextptr        
        DataZ        4                         
        DLabel       $mmgr-tags-prevptr        
        DataZ        4                         
        DLabel       $mmgr-tags-return         
        DataZ        4                         
        PushD        $mmgr-tags-return         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-size           
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-start          
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-available      
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-nextptr        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-prevptr        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-tags-prevptr        
        LoadI                                  
        PushD        $mmgr-tags-size           
        LoadI                                  
        PushD        $mmgr-tags-available      
        LoadI                                  
        PushD        $mmgr-tags-start          
        LoadI                                  
        Call         -mem-manager-one-tag      
        PushD        $mmgr-tags-nextptr        
        LoadI                                  
        PushD        $mmgr-tags-size           
        LoadI                                  
        PushD        $mmgr-tags-available      
        LoadI                                  
        PushD        $mmgr-tags-start          
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        Call         -mem-manager-one-tag      
        PushD        $mmgr-tags-return         
        LoadI                                  
        Return                                 
        Label        -mem-manager-one-tag      
        DLabel       $mmgr-onetag-return       
        DataZ        4                         
        DLabel       $mmgr-onetag-location     
        DataZ        4                         
        DLabel       $mmgr-onetag-available    
        DataZ        4                         
        DLabel       $mmgr-onetag-size         
        DataZ        4                         
        DLabel       $mmgr-onetag-pointer      
        DataZ        4                         
        PushD        $mmgr-onetag-return       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-location     
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-available    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-size         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-size         
        LoadI                                  
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        4                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $mmgr-onetag-available    
        LoadI                                  
        PushD        $mmgr-onetag-location     
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushD        $mmgr-onetag-return       
        LoadI                                  
        Return                                 
        Label        -mem-manager-allocate     
        DLabel       $mmgr-alloc-return        
        DataZ        4                         
        DLabel       $mmgr-alloc-size          
        DataZ        4                         
        DLabel       $mmgr-alloc-current-block 
        DataZ        4                         
        DLabel       $mmgr-alloc-remainder-block 
        DataZ        4                         
        DLabel       $mmgr-alloc-remainder-size 
        DataZ        4                         
        PushD        $mmgr-alloc-return        
        Exchange                               
        StoreI                                 
        PushI        18                        
        Add                                    
        PushD        $mmgr-alloc-size          
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Label        -mmgr-alloc-process-current 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        JumpFalse    -mmgr-alloc-no-block-works 
        Label        -mmgr-alloc-test-block    
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Subtract                               
        PushI        1                         
        Add                                    
        JumpPos      -mmgr-alloc-found-block   
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Jump         -mmgr-alloc-process-current 
        Label        -mmgr-alloc-found-block   
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        Call         -mem-manager-remove-block 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Subtract                               
        PushI        26                        
        Subtract                               
        JumpNeg      -mmgr-alloc-return-userblock 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Add                                    
        PushD        $mmgr-alloc-remainder-block 
        Exchange                               
        StoreI                                 
        PushD        $mmgr-alloc-size          
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        4                         
        Add                                    
        LoadI                                  
        Exchange                               
        Subtract                               
        PushD        $mmgr-alloc-remainder-size 
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushI        0                         
        PushI        0                         
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushD        $mmgr-alloc-size          
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushI        0                         
        PushI        0                         
        PushI        1                         
        PushD        $mmgr-alloc-remainder-block 
        LoadI                                  
        PushD        $mmgr-alloc-remainder-size 
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushD        $mmgr-alloc-remainder-block 
        LoadI                                  
        PushI        9                         
        Add                                    
        Call         -mem-manager-deallocate   
        Jump         -mmgr-alloc-return-userblock 
        Label        -mmgr-alloc-no-block-works 
        PushD        $mmgr-alloc-size          
        LoadI                                  
        PushD        $mmgr-newblock-size       
        Exchange                               
        StoreI                                 
        PushD        $heap-after-ptr           
        LoadI                                  
        PushD        $mmgr-newblock-block      
        Exchange                               
        StoreI                                 
        PushD        $mmgr-newblock-size       
        LoadI                                  
        PushD        $heap-after-ptr           
        LoadI                                  
        Add                                    
        PushD        $heap-after-ptr           
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushI        0                         
        PushI        0                         
        PushD        $mmgr-newblock-block      
        LoadI                                  
        PushD        $mmgr-newblock-size       
        LoadI                                  
        Call         -mem-manager-make-tags    
        PushD        $mmgr-newblock-block      
        LoadI                                  
        PushD        $mmgr-alloc-current-block 
        Exchange                               
        StoreI                                 
        Label        -mmgr-alloc-return-userblock 
        PushD        $mmgr-alloc-current-block 
        LoadI                                  
        PushI        9                         
        Add                                    
        PushD        $mmgr-alloc-return        
        LoadI                                  
        Return                                 
        Label        -mem-manager-deallocate   
        DLabel       $mmgr-dealloc-return      
        DataZ        4                         
        DLabel       $mmgr-dealloc-block       
        DataZ        4                         
        PushD        $mmgr-dealloc-return      
        Exchange                               
        StoreI                                 
        PushI        9                         
        Subtract                               
        PushD        $mmgr-dealloc-block       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushD        $heap-first-free          
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        0                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushD        $heap-first-free          
        LoadI                                  
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        PushI        1                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushI        1                         
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        8                         
        Add                                    
        Exchange                               
        StoreC                                 
        PushD        $mmgr-dealloc-block       
        LoadI                                  
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        PushD        $mmgr-dealloc-return      
        LoadI                                  
        Return                                 
        Label        -mem-manager-remove-block 
        DLabel       $mmgr-remove-return       
        DataZ        4                         
        DLabel       $mmgr-remove-block        
        DataZ        4                         
        DLabel       $mmgr-remove-prev         
        DataZ        4                         
        DLabel       $mmgr-remove-next         
        DataZ        4                         
        PushD        $mmgr-remove-return       
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        LoadI                                  
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-remove-prev         
        Exchange                               
        StoreI                                 
        PushD        $mmgr-remove-block        
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        LoadI                                  
        PushD        $mmgr-remove-next         
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-process-prev 
        PushD        $mmgr-remove-prev         
        LoadI                                  
        JumpFalse    -mmgr-remove-no-prev      
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushD        $mmgr-remove-prev         
        LoadI                                  
        Duplicate                              
        PushI        4                         
        Add                                    
        LoadI                                  
        Add                                    
        PushI        9                         
        Subtract                               
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Jump         -mmgr-remove-process-next 
        Label        -mmgr-remove-no-prev      
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushD        $heap-first-free          
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-process-next 
        PushD        $mmgr-remove-next         
        LoadI                                  
        JumpFalse    -mmgr-remove-done         
        PushD        $mmgr-remove-prev         
        LoadI                                  
        PushD        $mmgr-remove-next         
        LoadI                                  
        PushI        0                         
        Add                                    
        Exchange                               
        StoreI                                 
        Label        -mmgr-remove-done         
        PushD        $mmgr-remove-return       
        LoadI                                  
        Return                                 
        DLabel       $heap-memory              
