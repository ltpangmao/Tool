global element_info
set element_info to missing value
global element_list
set element_list to {}


---scan graph information
tell application id "OGfl"
	tell front window
		set selectedShapes to selection
		repeat with currentShape in selectedShapes
			set element_info to missing value -- empty variable
			-- first extract element info
			if (class of currentShape is shape) then
				set element_info to "element;"
				set element_info to element_info & id of currentShape as string
				--remove \n in name and produce a newText
				set oldText to text of currentShape as string
				set AppleScript's text item delimiters to {return & linefeed, return, linefeed, character id 8233, character id 8232}
				set newText to text items of oldText
				set AppleScript's text item delimiters to {" "}
				set newText to newText as text
				--tag empty
				if (newText is equal to "") then
					set newText to "empty"
				end if
				
				
				-- try to get some userdata
				try
					set owner to value of user data item "owner" of currentShape
					-- force evaluation of retval to trigger the error:
					set triggerError to owner
				on error
					set owner to null
				end try
				
				--continue to extract value
				set element_info to element_info & ";" & name of currentShape & ";" & newText & ";" & name of layer of currentShape & ";" & thickness of currentShape & ";" & double stroke of currentShape & ";" & size of currentShape & ";" & fill of currentShape & ";" & corner radius of currentShape & ";" & stroke pattern of currentShape & ";" & origin of currentShape & ";" & owner & ";" & name of canvas of currentShape & "
"
				set element_list to element_list & element_info -- add element to list
				-- then extract link info
			else if (class of currentShape is line) then
				--get head type
				set element_info to head type of currentShape
				try
					set element_info to element_info as string --test whether element_info is undefined...
				on error
					set element_info to "NoHead" -- if so name it as "no head"
				end try
				
				set element_info to "link;" & id of currentShape & ";" & element_info -- put it at the beginning, don't know how to test head type.
				
				--get link type
				set element_info to (element_info & ";" & line type of currentShape)
				--get source
				try
					set src_elem to source of currentShape
					set test to src_elem -- just used for test whether source is null, don't know other means yet.
					
					try
						set element_info to (element_info & ";" & id of src_elem)
					on error
						set element_info to (element_info & ";" & "NoSourceID")
					end try
				on error
					set element_info to (element_info & ";" & "NoSource")
				end try
				
				--get destination
				try
					set des_elem to destination of currentShape
					set test to des_elem -- just used for test whether destrination is null, don't know other means yet.
					
					try
						set element_info to (element_info & ";" & id of des_elem)
					on error
						set element_info to (element_info & ";" & "NoTargetID")
					end try
				on error
					set element_info to (element_info & ";" & "NoTarget")
				end try
				
				--get label
				try
					set mid_label to label of currentShape
					set test to mid_label -- just used for test whether label is null, don't know other means yet.
					try
						set element_info to (element_info & ";" & text of first item of mid_label)
					on error
						set element_info to (element_info & ";" & "NoLabelText")
					end try
				on error
					set element_info to (element_info & ";" & "NoLabel")
				end try
				
				--get other attributes
				set element_info to element_info & ";" & stroke pattern of currentShape & ";" & thickness of currentShape & ";" & head scale of currentShape & ";" & name of layer of currentShape & "
"
				set element_list to element_list & element_info -- add element to list
			end if
		end repeat
		
	end tell
end tell

--- output graph information
set append_data to false
--set target_file to ((path to desktop folder) & "tttest.txt") as string
set target_file to "Macintosh HD:Users:litong30:research:Trento:Workspace:research:TLSAF:applescript:graph_info.txt"
--clear file first
set empty_data to ""
write_to_file(empty_data, target_file, append_data)
--then output info
set append_data to true
repeat with current in element_list
	if (current is not missing value) then
		write_to_file(current, target_file, append_data)
	end if
end repeat

--Macintosh HD:Users:litong30:research:Trento:Workspace:research:TLSAF:applescript:import_info.scpt

--- output handler
on write_to_file(this_data, target_file, append_data) -- (string, file path as string, boolean)
	try
		set the target_file to the target_file as text
		set the open_target_file to open for access file target_file with write permission
		if append_data is false then set eof of the open_target_file to 0
		write this_data to the open_target_file starting at eof
		close access the open_target_file
		return true
	on error
		try
			close access file target_file
		end try
		return false
	end try
end write_to_file