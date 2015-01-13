import core.board.Board
import core.board.Cell
import core.board.Spell
import core.board.Unit
import core.graphics.scenes.BattleScene
import core.graphics.scenes.Scenes
import program.main.Util;

def onCheck(Spell spell, Board board, Cell target){
    target.getContentsType() == Cell.CONTENTS_UNIT && target.getUnit().getState() != Unit.STATE_DEAD
}

def onCheckAOE(Spell spell, Board board, Cell from, Cell to){
    from == to
}

def onCastBegin(Spell spell, Board board, Cell target){
	spell.caster.setFacing(target.unit)
	1.0f
}

def onCastEnd(Spell spell, Board board, Cell target){
    def attach = Util.getScene(Scenes.BATTLE, BattleScene.class).getSpatialByUnit(target.unit).node

	target.unit.doHeal(6)
	board.nextTurn()
	spell.putOnCoolDown()

    board.addEffect("heal-effect", attach)
}