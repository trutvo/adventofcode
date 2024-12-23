open Re

type direction = Undefined | Increasing | Decreasing | Invalid

let get_direction op c = match op with
  | None -> Undefined
  | Some p -> match p - c with
    | -1 | -2 | -3 -> Decreasing
    | 1 | 2 | 3 -> Increasing
    | _ -> Invalid

let get_reports d = 
  let re_report = Perl.compile_pat "\\d+" in
  let parse_line l = 
    Re.all re_report l |> List.map (fun groups -> Re.Group.get groups 0 |> int_of_string)
  in
  Io.Resource.read_lines d
    |> List.map parse_line

let validate_report r = 
  let rec is_save_loop d p l =
    match l with
    | [] -> (Ok `Safe)
    | h :: tl ->
      let new_d = get_direction p h in
      match (d, new_d) with
        | (Undefined, _) -> is_save_loop new_d (Some h) tl
        | (Increasing, Increasing) -> is_save_loop new_d (Some h) tl
        | (Decreasing, Decreasing) ->is_save_loop new_d (Some h) tl
        | (_, _) -> (Error ((List.length r) - (List.length l)))
  in
  is_save_loop Undefined None r

let is_report_safe r = (validate_report r) = (Ok `Safe)

let get_save_reports src =
  get_reports src |> List.filter is_report_safe

let remove_item pos r = (List.filteri (fun i _ -> i != pos) r) 

let get_save_reports_with_tolerance src =
  let is_safe rep =  
    match validate_report rep with
      | (Ok `Safe) -> true
      | (Error m) -> 
          is_report_safe (remove_item (m - 2) rep)
            || is_report_safe (remove_item (m - 1) rep)
            || is_report_safe (remove_item m rep)
            || is_report_safe (remove_item (m + 1) rep)
            || is_report_safe (remove_item (m + 2) rep)
  in
  get_reports src |> List.filter is_safe
