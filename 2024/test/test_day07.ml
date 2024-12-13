open OUnit2
open Aoc2024

let tests =
  "Day07" >::: [
    "a" >:: (fun _ -> 
      assert_equal ~printer:string_of_int 9 (List.length (Day07.Equation.load "day07-sample"));
      assert_equal ~printer:string_of_int 3749 (Day07.sum_all_valid_equations "day07-sample");
    );
    "b" >:: (fun _ -> 
      assert_equal ~printer:string_of_int 0 0;
    )
  ]

  let _ = run_test_tt_main tests
